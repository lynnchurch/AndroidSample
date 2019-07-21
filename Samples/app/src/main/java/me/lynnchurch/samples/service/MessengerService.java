package me.lynnchurch.samples.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import me.lynnchurch.samples.config.Constants;

public class MessengerService extends Service {
    private static final String TAG = MessengerService.class.getSimpleName();

    private static class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants
                        .MSG_FROM_CLIENT:
                    Log.i(TAG, "receive msg from client: " + msg.getData().getString(Constants.MSG));
                    Messenger messenger = msg.replyTo;
                    Message message = Message.obtain(null, Constants.MSG_FROM_SERVER);
                    Bundle data = new Bundle();
                    data.putString(Constants.MSG, "Hello, I'm from server.\n");
                    message.setData(data);
                    try {
                        messenger.send(message);
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(new MessageHandler()).getBinder();
    }
}
