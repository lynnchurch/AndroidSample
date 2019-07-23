package me.lynnchurch.assist;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class TargetApplication extends Application {
    private static final String TAG = TargetApplication.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        Log.i(TAG, "attachBaseContext");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.i(TAG, "onTrimMemory");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "onTerminate");
    }
}
