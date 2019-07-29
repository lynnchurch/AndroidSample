package me.lynnchurch.samples.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
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
    private static final String HEARTBEAT_MSG = "heartbeat"; // 心跳消息
    private static final String ALIVE_MSG = "alive"; // 活着的消息
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
    private Disposable mHeartbeatDisposable;
    private Type mSocketEventNetAddressType = new TypeToken<SocketEvent<NetAddress>>() {
    }.getType();

    @Override
    public void onCreate() {
        super.onCreate();
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
        mServerTcpPort = mRandom.nextInt(65536) % (65536 - 1024) + 1024;
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
                    SocketEvent<NetAddress> socketEvent = new SocketEvent<>(SocketEvent.CODE_TCP_SERVER_ADDRESS, "tcp server address", netAddress);
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

                    SocketEvent<NetAddress> socketEvent = new Gson().fromJson(receiveData, mSocketEventNetAddressType);

                    SocketAddress socketAddress = mClientUdpPacket.getSocketAddress();
                    Log.i(TAG, "socketAddress：" + socketAddress);

                    String msg = new StringBuilder("received packetData:\n").append(receiveData).append("\nfrom：" + socketAddress).toString();
                    Log.i(TAG, msg);
                    if (!mReceiveUdpBroadcastDisposable.isDisposed()) {
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
        stopHeartbeat();
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
                    mServerTcpSocket = new ServerSocket();
                }
                mServerTcpSocket.bind(new InetSocketAddress(mServerTcpPort));
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
                String data;
                StringBuilder dataSB = new StringBuilder();
                while ((data = br.readLine()) != null) {
                    dataSB.append(data);
                }
                data = dataSB.toString();
                Log.i(TAG, "server received data:\n" + data);
                mSocket.shutdownInput();

                os = mSocket.getOutputStream();
                pw = new PrintWriter(os);
                if (HEARTBEAT_MSG.equals(data)) {
                    pw.write(ALIVE_MSG);
                } else {
                    pw.write("response from server");
                }
                pw.flush();
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

    public void sendHeartbeatPacket(String serverIp, int serverPort) {
        mHeartbeatDisposable = Observable.interval(5, TimeUnit.SECONDS).observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    Socket socket = new Socket();
                    SocketAddress socketAddress = new InetSocketAddress(serverIp, serverPort);
                    socket.connect(socketAddress, 5000); // 心跳超时为5秒
                    Log.i(TAG, "set socket timeout.........................");
                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);

                    pw.write(HEARTBEAT_MSG);
                    pw.flush();
                    socket.shutdownOutput();

                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    String data;
                    StringBuilder dataSB = new StringBuilder();
                    while ((data = br.readLine()) != null) {
                        dataSB.append(data);
                    }

                    Log.i(TAG, "heartbeat received data:\n" + dataSB.toString());

                    os.close();
                    pw.close();
                    is.close();
                    isr.close();
                    br.close();
                }, throwable -> {
                    Log.e(TAG, throwable.getMessage(), throwable);
                }, () -> {
                });
    }

    private void stopHeartbeat() {
        if (null != mHeartbeatDisposable) {
            mHeartbeatDisposable.dispose();
        }
    }

    @Override
    public void onDestroy() {
        stopServer();
        stopClient();
        super.onDestroy();
    }
}
