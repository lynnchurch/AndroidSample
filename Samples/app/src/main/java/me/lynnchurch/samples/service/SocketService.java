package me.lynnchurch.samples.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN;

public abstract class SocketService extends Service {
    private static final String TAG = SocketService.class.getSimpleName();
    protected WifiManager mWifiManager;
    private String mIP = "0.0.0.0";
    private boolean mIsWifiEnabled;
    private boolean mIsNetConnected;

    @Override
    public void onCreate() {
        super.onCreate();
        registerWifiStateReceiver();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mIsWifiEnabled = mWifiManager.isWifiEnabled();
        if (!mIsWifiEnabled) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WIFI_STATE_UNKNOWN)) {
                    case WIFI_STATE_DISABLED: {
                        mIsWifiEnabled = false;
                        Log.i(TAG, "wifi disabled");
                        break;
                    }
                    case WIFI_STATE_DISABLING: {
                        Log.i(TAG, "wifi disabling");
                        break;
                    }
                    case WIFI_STATE_ENABLED: {
                        mIsWifiEnabled = true;
                        Log.i(TAG, "wifi enabled");
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        mIP = intToIp(wifiInfo.getIpAddress());
                        Log.i(TAG, "IP：" + mIP);
                        break;
                    }
                    case WIFI_STATE_ENABLING: {
                        Log.i(TAG, "wifi enabling");
                        break;
                    }
                    case WIFI_STATE_UNKNOWN: {
                        Log.i(TAG, "wifi state unknown");
                        break;
                    }
                }
            }
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    Log.i(TAG, "network sate change:" + state + " bssid:" + bssid);
                    if (state == NetworkInfo.State.CONNECTED) {
                        mIsNetConnected = true;
                        Log.i(TAG, "network connected");
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        mIP = intToIp(wifiInfo.getIpAddress());
                        Log.i(TAG, "IP：" + mIP);
                    } else if (state == NetworkInfo.State.DISCONNECTED) {
                        mIsNetConnected = false;
                        Log.i(TAG, "network disconnected");
                    }
                }
            }
        }
    };

    private void registerWifiStateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mBroadcastReceiver, filter);
    }

    public String getLocalIP() {
        return mIP;
    }

    public boolean isWifiEnabled() {
        return mIsWifiEnabled;
    }

    public boolean isNetConnected() {
        return mIsNetConnected;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
