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

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import me.lynnchurch.samples.config.Constants;

public class MessengerService extends Service {
    private static final String TAG = MessengerService.class.getSimpleName();

    private static class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handler thread:" + Thread.currentThread().getName());
            switch (msg.what) {
                case Constants
                        .MSG_FROM_CLIENT:
                    Log.i(TAG, "receive msg from client: " + msg.getData().getString(Constants.MSG));
                    Messenger messenger = msg.replyTo;
                    replyClient(messenger);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private static void replyClient(Messenger messenger) {
        Observable.intervalRange(0, 3, 0, 1, TimeUnit.SECONDS)
                .map(aLong -> {
                    Log.i(TAG, "map thread:" + Thread.currentThread().getName());
                    Message message = Message.obtain(null, Constants.MSG_FROM_SERVER);
                    Bundle data = new Bundle();
                    data.putString(Constants.MSG, "Hello, I'm from server. " + aLong + "\n");
                    message.setData(data);
                    return message;
                })
                .subscribe(message -> messenger.send(message)
                        , throwable -> Log.e(TAG, throwable.getMessage(), throwable), () -> {
                            Log.i(TAG, "subscribe thread:" + Thread.currentThread().getName());
                        });
    }

    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(new MessageHandler()).getBinder();
    }
}
