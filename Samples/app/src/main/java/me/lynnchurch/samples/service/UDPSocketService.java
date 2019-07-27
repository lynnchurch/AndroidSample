package me.lynnchurch.samples.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.lynnchurch.samples.event.SocketEvent;
import me.lynnchurch.samples.utils.RxBus;

public class UDPSocketService extends SocketService {
    public static final int CLIENT_LISTENING_UDP_PORT = 6666;
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
        try {
            mServerTcpPort = mRandom.nextInt(65536) % (65536 - 1024) + 1024;
            if(null == mServerSocket) {
                mServerSocket = new DatagramSocket();
            }
            mServerDisposable = Observable.interval(5, TimeUnit.SECONDS).observeOn(Schedulers.io())
                    .subscribe(aLong -> {
                        if (!isWifiEnabled() || !isNetConnected()) {
                            Log.i(TAG, "is waiting network available ...");
                            RxBus.getInstance().post(new SocketEvent("is waiting network available ..."));
                            return;
                        }
                        if (null == mServerPacket) {
                            mServerPacket = new DatagramPacket(mServerBuffer, mServerBuffer.length, InetAddress.getByName(getBroadcastIP()), CLIENT_LISTENING_UDP_PORT);
                        }
                        if (null == mServerSocket || mServerSocket.isClosed()) {
                            return;
                        }
                        mServerPacket.setData((getLocalIP() + ":" + mServerTcpPort).getBytes());
                        mServerSocket.send(mServerPacket);
                        Log.i(TAG, "server is broadcasting by udp ...");
                        RxBus.getInstance().post(new SocketEvent("server is broadcasting by udp ..."));
                    });
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void stopServer() {
        if (null != mServerDisposable) {
            mServerDisposable.dispose();
            new Thread(() -> {
                mServerSocket.disconnect();
                mServerSocket.close();
            }).start();
        }
        RxBus.getInstance().post(new SocketEvent("server is stopped"));
    }

    public void startClient() {
        try {
            if (null == mClientSocket) {
                mClientSocket = new DatagramSocket(CLIENT_LISTENING_UDP_PORT);
                mClientSocket.setReuseAddress(true);
                mClientPacket = new DatagramPacket(mClientBuffer, mClientBuffer.length);
            }
            mClientDisposable = Observable.interval(1, TimeUnit.SECONDS)
                    .observeOn(Schedulers.io())
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

                        SocketAddress socketAddress = mClientPacket.getSocketAddress();
                        Log.i(TAG, "socketAddress：" + socketAddress);

                        String msg = new StringBuilder("received packetData:\n").append(receiveData).append("\nfrom：" + socketAddress).toString();
                        Log.i(TAG, msg);
                        RxBus.getInstance().post(new SocketEvent(msg));
                    });
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void stopClient() {
        if (null != mClientDisposable) {
            mClientDisposable.dispose();
            new Thread(() -> {
                mClientSocket.disconnect();
                mClientSocket.close();
            });
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
        stopServer();
        stopClient();
        super.onDestroy();
    }
}
