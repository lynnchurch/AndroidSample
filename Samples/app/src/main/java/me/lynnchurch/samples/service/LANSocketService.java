package me.lynnchurch.samples.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.bean.NetAddress;
import me.lynnchurch.samples.event.SocketEvent;
import me.lynnchurch.samples.utils.RxBus;

public class LANSocketService extends SocketService {
    public static final int CLIENT_UDP_BROADCAST_LISTENING_PORT = 6666;
    private static final int BUFFER_LENGTH = 1024;
    private static final String TAG = LANSocketService.class.getSimpleName();
    private byte[] mServerBuffer = new byte[BUFFER_LENGTH];
    private DatagramSocket mServerUdpSocket; // 服务端UDP Socket
    private DatagramPacket mServerUdpPacket;
    private Disposable mSendUdpBroadcastDisposable;
    private DatagramSocket mClientUdpSocket; // 客户端UDP Socket
    private DatagramPacket mClientUdpPacket;
    private byte[] mClientUdpBuffer = new byte[BUFFER_LENGTH];
    private Disposable mReceiveUdpBroadcastDisposable;
    private Random mRandom = new Random(); // 用来生成TCP通信的端口号
    private int mServerTcpPort;
    private ServerSocket mServerTcpSocket;
    private boolean mIsServerRunning = false; // Server 是否还在运行
    private ExecutorService mServerTaskThreadPool;
    private boolean mIsClientRunning; // Client 是否还在运行
    private Socket mClientTcpSocket; // 用于心跳检测的Socket
    private OutputStream mClientTcpOutputStream;
    private PrintWriter mClientTcpPrintWriter;
    private InputStream mClientTcpInputStream;
    private InputStreamReader mClientTcpInputStreamReader;
    private BufferedReader mClientTcpBufferedReader;
    private long mLastReceivedHeartbeatResponseTime; // 最近收到心跳响应的时间

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
        public LANSocketService getService() {
            return LANSocketService.this;
        }
    }

    public void startServer() {
        mIsServerRunning = true;
        sendUdpBroadcast();
        startTcpServer();
    }

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
                    SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_SERVER_ADDRESS, "tcp server address", new Gson().toJson(netAddress));
                    mServerUdpPacket.setData(new Gson().toJson(socketEvent).getBytes());
                    mServerUdpSocket.send(mServerUdpPacket);
                    Log.i(TAG, "server is broadcasting by udp ...");
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

    public void stopServer() {
        mIsServerRunning = false;
        if (null != mSendUdpBroadcastDisposable) {
            mSendUdpBroadcastDisposable.dispose();
        }
        RxBus.getInstance().post(new SocketEvent("server is stopped"));
    }

    public void startClient() {
        mIsClientRunning = true;
        receiveUdpBroadcast();
    }

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
                    Log.i(TAG, "server udp socketAddress：" + socketAddress);

                    String msg = new StringBuilder("udp received packetData:\n").append(receiveData).append("\nfrom：" + socketAddress).toString();
                    Log.i(TAG, msg);
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

    public void stopClient() {
        if (null != mReceiveUdpBroadcastDisposable) {
            mReceiveUdpBroadcastDisposable.dispose();
        }
        mIsClientRunning = false;
        RxBus.getInstance().post(new SocketEvent("client is stopped"));
    }

    protected String getBroadcastIP() {
        String ip = getLocalIP();
        ip = ip.substring(0, ip.lastIndexOf(".")) + ".255";
        return ip;
    }

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
                    mServerTaskThreadPool.submit(new ServerTask(clientSocket));
                    Log.i(TAG, "client ip：" + clientSocket.getInetAddress().getHostAddress());
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).start();
    }

    private class ServerTask implements Runnable {
        private Socket mSocket;

        public ServerTask(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
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

                while (mIsServerRunning) {
                    String data = br.readLine();
                    Log.i(TAG, "server received tcp data：" + data);
                    SocketEvent socketEvent = new Gson().fromJson(data, SocketEvent.class);
                    if (null != socketEvent && socketEvent.code == SocketEvent.CODE_TCP_HEARTBEAT) {
                        sendHeartbeatResponse(pw);
                    }
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

    private void sendHeartbeatResponse(PrintWriter printWriter) {
        SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_SERVER_ALIVE, null, null);
        printWriter.println(new Gson().toJson(socketEvent));
        printWriter.flush();
    }

    public void startTcpClient(String serverIp, int serverPort) {
        new Thread(() -> {
            try {
                mClientTcpSocket = new Socket(serverIp, serverPort);

                mClientTcpOutputStream = mClientTcpSocket.getOutputStream();
                mClientTcpPrintWriter = new PrintWriter(mClientTcpOutputStream);

                mClientTcpInputStream = mClientTcpSocket.getInputStream();
                mClientTcpInputStreamReader = new InputStreamReader(mClientTcpInputStream);
                mClientTcpBufferedReader = new BufferedReader(mClientTcpInputStreamReader);

                handleReceivedTcpData();
                while (mIsClientRunning) {
                    sendHeartbeatMsg();
                    if (0 == mLastReceivedHeartbeatResponseTime) {
                        mLastReceivedHeartbeatResponseTime = System.currentTimeMillis();
                    }
                    // 超过7秒还未收到服务器的心跳响应则认为服务器已挂
                    if (System.currentTimeMillis() - mLastReceivedHeartbeatResponseTime > 7000) {
                        mIsClientRunning = false;
                        Log.i(TAG, "server is dead");
                    }
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

    private void handleReceivedTcpData() {
        new Thread(() -> {
            while (mIsClientRunning) {
                try {
                    String data = mClientTcpBufferedReader.readLine();
                    Log.i(TAG, "client received tcp data：" + data);
                    SocketEvent socketEvent = new Gson().fromJson(data, SocketEvent.class);
                    // 更新最近收到心跳响应的时间
                    if (null != socketEvent && socketEvent.code == SocketEvent.CODE_TCP_SERVER_ALIVE) {
                        mLastReceivedHeartbeatResponseTime = System.currentTimeMillis();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }).start();
    }

    private void sendHeartbeatMsg() {
        SocketEvent socketEvent = new SocketEvent(SocketEvent.CODE_TCP_HEARTBEAT, null, null);
        mClientTcpPrintWriter.println(new Gson().toJson(socketEvent));
        mClientTcpPrintWriter.flush();
    }

    @Override
    public void onDestroy() {
        stopServer();
        stopClient();
        super.onDestroy();
    }
}
