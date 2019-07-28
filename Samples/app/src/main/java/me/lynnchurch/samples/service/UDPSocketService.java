package me.lynnchurch.samples.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.bean.NetAddress;
import me.lynnchurch.samples.event.SocketEvent;
import me.lynnchurch.samples.utils.RxBus;

public class UDPSocketService extends SocketService {
    public static final int CLIENT_UDP_BROADCAST_LISTENING_PORT = 6666;
    private static final int BUFFER_LENGTH = 1024;
    private static final String TAG = UDPSocketService.class.getSimpleName();
    private DatagramSocket mServerSocket; // 服务端Socket
    private byte[] mServerBuffer = new byte[BUFFER_LENGTH];
    private DatagramPacket mServerPacket;
    private Disposable mServerDisposable;
    private DatagramSocket mClientSocket; // 客户端Socket
    private DatagramPacket mClientPacket;
    private byte[] mClientBuffer = new byte[BUFFER_LENGTH];
    private Disposable mClientDisposable;
    private Random mRandom = new Random(); // 用来生成TCP通信的端口号
    private int mServerTcpPort;
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
        public UDPSocketService getService() {
            return UDPSocketService.this;
        }
    }

    public void startServer() {
        sendUdpBroadcast();
    }

    private void sendUdpBroadcast() {
        mServerTcpPort = mRandom.nextInt(65536) % (65536 - 1024) + 1024;
        if (null == mServerSocket) {
            try {
                mServerSocket = new DatagramSocket();
                mServerPacket = new DatagramPacket(mServerBuffer, mServerBuffer.length, InetAddress.getByName(getBroadcastIP()), CLIENT_UDP_BROADCAST_LISTENING_PORT);
            } catch (UnknownHostException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (SocketException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        mServerDisposable = Observable.interval(3, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    if (!isWifiEnabled() || !isNetConnected()) {
                        Log.i(TAG, "is waiting network available ...");
                        RxBus.getInstance().post(new SocketEvent("is waiting network available ..."));
                        return;
                    }
                    Log.i(TAG, "mServerSocket:" + mServerSocket);
                    if (null == mServerSocket || mServerSocket.isClosed()) {
                        return;
                    }
                    NetAddress netAddress = new NetAddress(getLocalIP(), mServerTcpPort);
                    SocketEvent<NetAddress> socketEvent = new SocketEvent<>(SocketEvent.CODE_TCP_S1ERVER_ADDRESS, "tcp server address", netAddress);
                    mServerPacket.setData(new Gson().toJson(socketEvent).getBytes());
                    mServerSocket.send(mServerPacket);
                    Log.i(TAG, "server is broadcasting by udp ...");
                    if (!mServerDisposable.isDisposed()) {
                        RxBus.getInstance().post(new SocketEvent("server is broadcasting by udp ..."));
                    }
                });
    }

    public void stopServer() {
        if (null != mServerDisposable) {
            mServerDisposable.dispose();
            new Thread(() -> mServerSocket.disconnect()).start();
        }
        RxBus.getInstance().post(new SocketEvent("server is stopped"));
    }

    public void startClient() {
        receiveUdpBroadcast();
    }

    private void receiveUdpBroadcast() {
        if (null == mClientSocket) {
            try {
                mClientSocket = new DatagramSocket(CLIENT_UDP_BROADCAST_LISTENING_PORT);
                mClientSocket.setReuseAddress(true);
            } catch (SocketException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            mClientPacket = new DatagramPacket(mClientBuffer, mClientBuffer.length);
        }
        mClientDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    if (!isWifiEnabled() || !isNetConnected()) {
                        Log.i(TAG, "is waiting network available ...");
                        RxBus.getInstance().post(new SocketEvent("is waiting network available ..."));
                        return;
                    }
                    if (null == mClientSocket || mClientSocket.isClosed()) {
                        return;
                    }
                    mClientSocket.receive(mClientPacket);
                    String receiveData = new String(mClientPacket.getData(), mClientPacket.getOffset(), mClientPacket.getLength());

                    SocketEvent<NetAddress> socketEvent = new Gson().fromJson(receiveData, mSocketEventNetAddressType);

                    SocketAddress socketAddress = mClientPacket.getSocketAddress();
                    Log.i(TAG, "socketAddress：" + socketAddress);

                    String msg = new StringBuilder("received packetData:\n").append(receiveData).append("\nfrom：" + socketAddress).toString();
                    Log.i(TAG, msg);
                    if (!mClientDisposable.isDisposed()) {
                        RxBus.getInstance().post(socketEvent);
                    }
                });
    }

    public void stopClient() {
        if (null != mClientDisposable) {
            mClientDisposable.dispose();
            new Thread(() -> mClientSocket.disconnect());
        }
        RxBus.getInstance().post(new SocketEvent("client is stopped"));
    }

    protected String getBroadcastIP() {
        String ip = getLocalIP();
        ip = ip.substring(0, ip.lastIndexOf(".")) + ".255";
        return ip;
    }

    @Override
    public void onDestroy() {
        if (null != mServerSocket) {
            mServerSocket.close();
            mServerSocket = null;
        }
        if (null != mClientSocket) {
            mClientSocket.close();
            mClientSocket = null;
        }
        super.onDestroy();
    }
}
