package me.lynnchurch.assist.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import me.lynnchurch.assist.R;
import me.lynnchurch.assist.config.Constants;

public class MessengerActivity extends BaseActivity {
    private static final String TAG = MessengerActivity.class.getSimpleName();
    private final RxPermissions rxPermissions = new RxPermissions(this);
    private Messenger mMessenger;
    private TextView tvContent;
    private StringBuilder mContent = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Assist Messenger");
        requestPermissions();
        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_messenger;
    }

    private void requestPermissions() {
        rxPermissions.request("lynnchurch.permission.MESSENGER")
                .subscribe(granted -> {
                    if (granted) {
                        Log.i(TAG, "MESSENGER is granted");
                        Intent intent = new Intent();
                        intent.setClassName("me.lynnchurch.samples", "me.lynnchurch.samples.service.MessengerService");
                        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
                    } else {
                        Log.i(TAG, "MESSENGER is not granted");
                    }
                });
    }

    private void initView() {
        tvContent = findViewById(R.id.tvContent);
        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private Handler mServerMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_SERVER:
                    String serverMsg = msg.getData().getString(Constants.MSG);
                    mContent.append("server: " + serverMsg);
                    tvContent.setText(mContent.toString());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public void sendMsgToServer(View v) {
        if (null == mMessenger) {
            toast("客户端还未准备好", Toast.LENGTH_LONG);
            return;
        }

        Message message = Message.obtain(null, Constants.MSG_FROM_CLIENT);
        Bundle data = new Bundle();
        String msg = "Hello, I'm from client.\n";
        data.putString(Constants.MSG, msg);
        message.setData(data);
        message.replyTo = new Messenger(mServerMsgHandler);
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        mContent.append("client: " + msg);
        tvContent.setText(mContent.toString());
    }

    @Override
    protected void onDestroy() {
        if (rxPermissions.isGranted("lynnchurch.permission.MESSENGER")) {
            unbindService(mServiceConnection);
        }
        super.onDestroy();
    }
}
