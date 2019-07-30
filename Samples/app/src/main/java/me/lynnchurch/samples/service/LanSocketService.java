package me.lynnchurch.samples.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.bean.NetAddress;
import me.lynnchurch.samples.bean.SocketMessage;
import me.lynnchurch.samples.event.SocketEvent;
import me.lynnchurch.samples.utils.RxBus;

public class LanSocketService extends SocketService {
    public static final int CLIENT_UDP_BROADCAST_LISTENING_PORT = 6666;
    private static final int BUFFER_LENGTH = 1024;
    private static final String TAG = LanSocketService.class.getSimpleName();
    private byte[] mServerBuffer = new byte[BUFFER_LENGTH];
    private DatagramSocket mServerUdpSocket; // 服务端UDP Socket
    private DatagramPacket mServerUdpPacket;
    private Disposable mSendUdpBroadcastDisposable;
    private DatagramSocket mClientUdpSocket; // 客户端UDP Socket
    private DatagramPacket mClientUdpPacket;
    private byte[] mClientUdpBuffer = new byte[BUFFER_LENGTH];
    private Disposable mReceiveUdpBroadcastDisposable;
    private Random mRandom = new Random(); // 用来生成TCP通信的端口号
    private int mServerTcpPort; // 服务端监听的端口
    private ServerSocket mServerTcpSocket;
    private boolean mIsServerRunning = false; // Server 是否还在运行
    private ExecutorService mServerTaskThreadPool;
    private Map<NetAddress, Boolean> mClientStatusMap = new HashMap<>(); // 客户端状态（true已连接，false已断开连接）
    private Map<NetAddress, BufferedReader> mClientBufferedReaderMap = new HashMap<>(); // 用于接收客户端数据的输入流
    private Map<NetAddress, PrintWriter> mClientPrintWriterMap = new HashMap<>(); // 用于向客户端发送数据的输出流
    private Map<NetAddress, Long> mClientHeartbeatResponseTimeMap = new HashMap<>(); // 服务端最近收到心跳响应的时间
    private boolean mIsClientRunning; // Client 是否还在运行
    private Socket mClientTcpSocket; // 用于心跳检测的Socket
    private OutputStream mClientTcpOutputStream;
    private PrintWriter mClientTcpPrintWriter;
    private InputStream mClientTcpInputStream;
    private InputStreamReader mClientTcpInputStreamReader;
    private BufferedReader mClientTcpBufferedReader;
    private int mClientTcpPort; // 客户端TCP通信端口
    private String mConnectedServerIp; // 所连接的服务端IP地址
    private int mConnectedServerPort; // 所连接的服务端端口
    private long mClientLastReceivedHeartbeatResponseTime; // 客户端最近收到心跳响应的时间

    @Override
    public void onCreate() {
        super.onCreate();
        mServerTcpPort = mRandom.nextInt(65536) % (65536 - 1024) + 1024;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SocketBinder();
    }

    public class SocketBinder extends Binder {
        public LanSocketService getService() {
            return LanSocketService.this;
        }
    }

    /**
     * 启动服务端
     */
    public void startServer() {
        mIsServerRunning = true;
        sendUdpBroadcast();
        startTcpServer();
    }

    /**
     * 发送UDP广播
     */
    private void sendUdpBroadcast() {
        try {
            mServerUdpPacket = new DatagramPacket(mServerBuffer, mServerBuffer.length, InetAddress.getByName(getBroadcastIP()), CLIENT_UDP_BROADCAST_LISTENING_PORT);
        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        mSendUdpBroadcastDisposable = Observable.interval(3, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    if (!isWifiEnabled() || !isNetConnected()) {
                        Log.i(TAG, "is waiting network available ...");
                        RxBus.getInstance().post(new SocketEvent("is waiting network available ..."));
                        return;
                    }

                    mServerUdpSocket = new DatagramSocket();
                    NetAddress netAddress = new NetAddress(getLocalIP(), mServerTcpPort);
                    SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_SERVER_ADDRESS, "tcp server address", new Gson().toJson(netAddress).getBytes());
                    mServerUdpPacket.setData(new Gson().toJson(socketEvent).getBytes());
                    mServerUdpSocket.send(mServerUdpPacket);
//                    Log.i(TAG, "server is broadcasting by udp ...");
                    if (!mSendUdpBroadcastDisposable.isDisposed()) {
                        RxBus.getInstance().post(new SocketEvent("server is broadcasting by udp ..."));
                    }
                    mServerUdpSocket.close();
                }, throwable -> {
                    Log.e(TAG, throwable.getMessage(), throwable);
                    mServerUdpSocket.close();
                }, () -> {
                });
    }


    /**
     * 接收服务端的UDP广播
     */
    private void receiveUdpBroadcast() {
        mClientUdpPacket = new DatagramPacket(mClientUdpBuffer, mClientUdpBuffer.length);
        mReceiveUdpBroadcastDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    if (!isWifiEnabled() || !isNetConnected()) {
                        Log.i(TAG, "is waiting network available ...");
                        RxBus.getInstance().post(new SocketEvent("is waiting network available ..."));
                        return;
                    }
                    mClientUdpSocket = new DatagramSocket(CLIENT_UDP_BROADCAST_LISTENING_PORT);
                    mClientUdpSocket.receive(mClientUdpPacket);
                    String receiveData = new String(mClientUdpPacket.getData(), mClientUdpPacket.getOffset(), mClientUdpPacket.getLength());

                    SocketEvent socketEvent = new Gson().fromJson(receiveData, SocketEvent.class);

                    SocketAddress socketAddress = mClientUdpPacket.getSocketAddress();
//                    Log.i(TAG, "server udp socketAddress：" + socketAddress);

                    String msg = new StringBuilder("udp received packetData:\n").append(receiveData).append("\nfrom：" + socketAddress).toString();
//                    Log.i(TAG, msg);
                    if (null != socketEvent && !mReceiveUdpBroadcastDisposable.isDisposed()) {
                        RxBus.getInstance().post(socketEvent);
                    }
                    mClientUdpSocket.close();
                }, throwable -> {
                    Log.e(TAG, throwable.getMessage(), throwable);
                    mClientUdpSocket.close();
                }, () -> {
                });
    }

    /**
     * 启动客户端
     */
    public void startClient() {
        mIsClientRunning = true;
        receiveUdpBroadcast();
    }

    /**
     * 停止客户端
     */
    public void stopClient() {
        if (null != mReceiveUdpBroadcastDisposable) {
            mReceiveUdpBroadcastDisposable.dispose();
        }
        mClientLastReceivedHeartbeatResponseTime = 0;
        mIsClientRunning = false;
        RxBus.getInstance().post(new SocketEvent("client is stopped"));
    }

    /**
     * 获取广播地址
     *
     * @return
     */
    protected String getBroadcastIP() {
        String ip = getLocalIP();
        ip = ip.substring(0, ip.lastIndexOf(".")) + ".255";
        return ip;
    }

    /**
     * 启动TCP服务端
     */
    private void startTcpServer() {
        new Thread(() -> {
            try {
                if (null == mServerTcpSocket) {
                    mServerTcpSocket = new ServerSocket(mServerTcpPort);
                }
                Log.i(TAG, "tcp server is running ...");
                Socket clientSocket;
                if (null == mServerTaskThreadPool) {
                    mServerTaskThreadPool = Executors.newCachedThreadPool();
                }
                while (mIsServerRunning) {
                    clientSocket = mServerTcpSocket.accept();
                    mServerTaskThreadPool.submit(new ServerCommunicationTask(clientSocket));
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).start();
    }

    /**
     * 停止服务端
     */
    public void stopServer() {
        mIsServerRunning = false;
        if (null != mSendUdpBroadcastDisposable) {
            mSendUdpBroadcastDisposable.dispose();
        }
        if (null != mServerTaskThreadPool) {
            mServerTaskThreadPool.shutdown();
        }
        RxBus.getInstance().post(new SocketEvent("server is stopped"));
    }

    /**
     * 用来与已建立连接的客户端进行通讯
     */
    private class ServerCommunicationTask implements Runnable {
        private Socket mSocket;

        public ServerCommunicationTask(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            String clientAddress = mSocket.getInetAddress().getHostAddress();
            Log.i(TAG, "new client connected：" + clientAddress);
            NetAddress clientNetAddress = new NetAddress(clientAddress, mSocket.getPort());
            SocketEvent newClientConnectedSocketEvent = new SocketEvent(SocketEvent.CODE_TCP_NEW_CLIENT_CONNECTED, null, new Gson().toJson(clientNetAddress).getBytes());
            RxBus.getInstance().post(newClientConnectedSocketEvent);

            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            OutputStream os = null;
            PrintWriter pw = null;
            try {
                is = mSocket.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                os = mSocket.getOutputStream();
                pw = new PrintWriter(os);

                mClientStatusMap.put(clientNetAddress, true);
                mClientBufferedReaderMap.put(clientNetAddress, br);
                mClientPrintWriterMap.put(clientNetAddress, pw);

                serverSendHeartbeatMsg(clientNetAddress);

                while (mIsServerRunning && mClientStatusMap.get(clientNetAddress)) {
                    String data = br.readLine();
                    Log.i(TAG, "server received tcp data：" + data);
                    SocketEvent socketEvent = new Gson().fromJson(data, SocketEvent.class);
                    Log.i(TAG, "socketEvent1:" + socketEvent);
                    if (null != socketEvent) {
                        // 更新最近收到心跳响应的时间
                        mClientHeartbeatResponseTimeMap.put(clientNetAddress, System.currentTimeMillis());
                        switch (socketEvent.code) {
                            case SocketEvent.CODE_TCP_HEARTBEAT:
                                serverSendHeartbeatResponse(pw);
                                break;
                            case SocketEvent.CODE_TCP_CLIENT_ALIVE:
                                break;
                            case SocketEvent.CODE_TCP_TEXT:
                                serverHandleTextMsg(clientNetAddress, socketEvent);
                            default:
                        }
                    }
                    checkClientAlive(clientNetAddress);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                try {
                    if (pw != null) {
                        pw.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                    if (isr != null) {
                        isr.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                    if (mSocket != null) {
                        mSocket.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 服务端处理文本消息
     *
     * @param fromAddress
     * @param socketEvent
     */
    private void serverHandleTextMsg(NetAddress fromAddress, SocketEvent socketEvent) {
        SocketMessage socketMessage = new SocketMessage(fromAddress, null, new String(socketEvent.data), System.currentTimeMillis());
        RxBus.getInstance().post(socketMessage);
    }

    /**
     * 检查客户端是否还活着
     *
     * @param netAddress
     */
    private void checkClientAlive(NetAddress netAddress) {
        Log.i(TAG, "checkClientAlive");
        Long serverLastReceivedHeartbeatResponseTime = mClientHeartbeatResponseTimeMap.get(netAddress);
        if (null == serverLastReceivedHeartbeatResponseTime) {
            serverLastReceivedHeartbeatResponseTime = System.currentTimeMillis();
            mClientHeartbeatResponseTimeMap.put(netAddress, serverLastReceivedHeartbeatResponseTime);
        }
        // 超过1秒还未收到客户端的心跳响应则认为客户端已挂
        long timeout = System.currentTimeMillis() - serverLastReceivedHeartbeatResponseTime;
        Log.i(TAG, "timeout:" + timeout);
        if (timeout > 1000) {
            mClientStatusMap.put(netAddress, false);
            mClientPrintWriterMap.remove(netAddress);
            mClientBufferedReaderMap.remove(netAddress);
            mClientHeartbeatResponseTimeMap.remove(netAddress);
            RxBus.getInstance().post(new SocketEvent(SocketEvent.CODE_TCP_CLIENT_DEAD, null, null));
            Log.i(TAG, "client:" + netAddress + " is dead");
        }
    }

    /**
     * 服务端发送心跳消息
     *
     * @param netAddress 接收心跳消息的客户端
     */
    private void serverSendHeartbeatMsg(NetAddress netAddress) {
        mServerTaskThreadPool.submit(() -> {
            while (mIsServerRunning && mClientStatusMap.get(netAddress)) {
                SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_HEARTBEAT, null, null);
                PrintWriter printWriter = mClientPrintWriterMap.get(netAddress);
                printWriter.println(new Gson().toJson(socketEvent));
                printWriter.flush();
                Log.i(TAG, "serverSendHeartbeatMsg");
                SystemClock.sleep(5000);
            }
        });
    }

    /**
     * 服务端发送客户端心跳检测的响应
     *
     * @param printWriter
     */
    private void serverSendHeartbeatResponse(PrintWriter printWriter) {
        SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_SERVER_ALIVE, null, null);
        printWriter.println(new Gson().toJson(socketEvent));
        printWriter.flush();
        Log.i(TAG, "serverSendHeartbeatResponse");
    }

    /**
     * 服务端发送文本消息
     *
     * @param text
     */
    public void serverSendTextMsg(String text) {
        if (!mIsServerRunning || TextUtils.isEmpty(text)) {
            return;
        }

        new Thread(() -> {
            Collection<PrintWriter> printWriters = mClientPrintWriterMap.values();
            Iterator<PrintWriter> iterator = printWriters.iterator();
            SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_TEXT, null, text.getBytes());
            while (iterator.hasNext()) {
                PrintWriter printWriter = iterator.next();
                printWriter.println(new Gson().toJson(socketEvent));
                printWriter.flush();
            }

            // 主要用于消息在UI显示
            NetAddress netAddress = new NetAddress(getLocalIP(), mServerTcpPort);
            SocketMessage socketMessage = new SocketMessage(null, netAddress, text, System.currentTimeMillis());
            RxBus.getInstance().post(socketMessage);
        }).start();
    }

    /**
     * 启动TCP客户端
     *
     * @param serverIp
     * @param serverPort
     */
    public void startTcpClient(String serverIp, int serverPort) {
        mConnectedServerIp = serverIp;
        mConnectedServerPort = serverPort;
        new Thread(() -> {
            try {
                mClientTcpSocket = new Socket(serverIp, serverPort);

                mClientTcpPort = mClientTcpSocket.getLocalPort();

                mClientTcpOutputStream = mClientTcpSocket.getOutputStream();
                mClientTcpPrintWriter = new PrintWriter(mClientTcpOutputStream);

                mClientTcpInputStream = mClientTcpSocket.getInputStream();
                mClientTcpInputStreamReader = new InputStreamReader(mClientTcpInputStream);
                mClientTcpBufferedReader = new BufferedReader(mClientTcpInputStreamReader);

                clientHandleReceivedTcpData();
                while (mIsClientRunning) {
                    clientSendHeartbeatMsg();
                    SystemClock.sleep(5000);
                }
                if (null != mClientTcpSocket) {
                    mClientTcpSocket.close();
                }
                if (null != mClientTcpOutputStream) {
                    mClientTcpOutputStream.close();
                }
                if (null != mClientTcpPrintWriter) {
                    mClientTcpPrintWriter.close();
                }
                if (null != mClientTcpInputStream) {
                    mClientTcpInputStream.close();
                }
                if (null != mClientTcpInputStreamReader) {
                    mClientTcpInputStreamReader.close();
                }
                if (null != mClientTcpBufferedReader) {
                    mClientTcpBufferedReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                mIsClientRunning = false;
            }
        }).start();
    }

    /**
     * 检查服务端是否还活着
     */
    private void checkServerAlive() {
        if (0 == mClientLastReceivedHeartbeatResponseTime) {
            mClientLastReceivedHeartbeatResponseTime = System.currentTimeMillis();
        }
        // 超过1秒还未收到服务端的心跳响应则认为服务端已挂
        long timeout = System.currentTimeMillis() - mClientLastReceivedHeartbeatResponseTime;
        Log.i(TAG, "timeout:" + timeout);
        if (timeout > 1000) {
            mIsClientRunning = false;
            SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_SERVER_DEAD, null, null);
            RxBus.getInstance().post(socketEvent);
            Log.i(TAG, "server is dead");
        }
    }

    /**
     * 客户端发送心跳消息
     */
    private void clientSendHeartbeatMsg() {
        SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_HEARTBEAT, null, null);
        mClientTcpPrintWriter.println(new Gson().toJson(socketEvent));
        mClientTcpPrintWriter.flush();
        Log.i(TAG, "clientSendHeartbeatMsg");
    }

    /**
     * 客户端对接收的TCP数据进行处理
     */
    private void clientHandleReceivedTcpData() {
        new Thread(() -> {
            while (mIsClientRunning) {
                try {
                    String data = mClientTcpBufferedReader.readLine();
                    Log.i(TAG, "client received tcp data：" + data);
                    SocketEvent socketEvent = new Gson().fromJson(data, SocketEvent.class);
                    if (null != socketEvent) {
                        // 更新最近收到心跳响应的时间
                        mClientLastReceivedHeartbeatResponseTime = System.currentTimeMillis();
                        switch (socketEvent.code) {
                            case SocketEvent.CODE_TCP_SERVER_ALIVE:
                                break;
                            case SocketEvent.CODE_TCP_HEARTBEAT:
                                clientSendHeartbeatResponse();
                                break;
                            case SocketEvent.CODE_TCP_TEXT:
                                clientHandleTextMsg(socketEvent);
                                break;
                            default:
                        }
                    }
                    checkServerAlive();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }).start();
    }

    /**
     * 客户端处理文本消息
     *
     * @param socketEvent
     */
    private void clientHandleTextMsg(SocketEvent socketEvent) {
        NetAddress fromAddress = new NetAddress(mConnectedServerIp, mConnectedServerPort);
        SocketMessage socketMessage = new SocketMessage(fromAddress, null, new String(socketEvent.data), System.currentTimeMillis());
        RxBus.getInstance().post(socketMessage);
    }

    /**
     * 客户端发送服务端心跳检测的响应
     */
    private void clientSendHeartbeatResponse() {
        SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_CLIENT_ALIVE, null, null);
        mClientTcpPrintWriter.println(new Gson().toJson(socketEvent));
        mClientTcpPrintWriter.flush();
        Log.i(TAG, "clientSendHeartbeatResponse");
    }

    /**
     * 客户端发送文本消息
     *
     * @param text
     */
    public void clientSendTextMsg(String text) {
        if (!mIsClientRunning || TextUtils.isEmpty(text)) {
            return;
        }
        if (null == mClientTcpPrintWriter) {
            return;
        }
        new Thread(() -> {
            SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_TEXT, null, text.getBytes());
            mClientTcpPrintWriter.println(new Gson().toJson(socketEvent));
            mClientTcpPrintWriter.flush();

            // 主要用于消息在UI显示
            NetAddress netAddress = new NetAddress(getLocalIP(), mClientTcpPort);
            SocketMessage socketMessage = new SocketMessage(null, netAddress, text, System.currentTimeMillis());
            RxBus.getInstance().post(socketMessage);
        }).start();
    }

    @Override
    public void onDestroy() {
        stopServer();
        stopClient();
        super.onDestroy();
    }
}
