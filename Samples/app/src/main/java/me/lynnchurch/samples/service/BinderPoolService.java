package me.lynnchurch.samples.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import me.lynnchurch.samples.aidl.BinderManager;
import me.lynnchurch.samples.aidl.IBinderPool;

public class BinderPoolService extends Service {
    public BinderPoolService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new BinderManager.BinderPoolImpl();
    }

}
