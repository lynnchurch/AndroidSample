package me.lynnchurch.samples.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

import me.lynnchurch.samples.service.BinderPoolService;

public class BinderManager {
    private static final String TAG = BinderManager.class.getSimpleName();
    public static final int BINDER_MODULE_A = 0;
    public static final int BINDER_MODULE_B = 1;

    private static volatile BinderManager mInstance;
    private Context mContext;
    private CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private IBinderPool mBinderPool;

    private BinderManager(Context context) {
        mContext = context.getApplicationContext();
        bindService();
    }

    public static BinderManager getInstance(Context context) {
        if (null == mInstance) {
            synchronized (BinderManager.class) {
                if (null == mInstance) {
                    mInstance = new BinderManager(context);
                }
            }
        }
        return mInstance;
    }

    private void bindService() {
        mContext.bindService(new Intent(mContext, BinderPoolService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            mCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBinderPool = null;
            bindService();
        }
    };

    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        if (null != mBinderPool) {
            try {
                binder = mBinderPool.queryBinder(binderCode);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return binder;
    }

    public static class BinderPoolImpl extends IBinderPool.Stub {

        @Override
        public IBinder queryBinder(int binderCode) {
            IBinder binder = null;
            switch (binderCode) {
                case BINDER_MODULE_A:
                    binder = new ModuleAImpl();
                    break;
                case BINDER_MODULE_B:
                    binder = new ModuleBImpl();
                    break;
            }
            return binder;
        }
    }

    public static class ModuleAImpl extends IModuleA.Stub {

        @Override
        public String hello(String name) {
            return "Hello " + name + " ! (from Module A)";
        }
    }

    public static class ModuleBImpl extends IModuleB.Stub {

        @Override
        public String hello(String name) {
            return "Hello " + name + " ! (from Module B)";
        }
    }
}
