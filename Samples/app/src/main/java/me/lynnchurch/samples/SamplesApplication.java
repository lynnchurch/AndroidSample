package me.lynnchurch.samples;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import java.util.logging.Logger;

public class SamplesApplication extends Application {
    private final Logger logger = Logger.getLogger(SamplesApplication.class.getSimpleName());

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        logger.info("attachBaseContext");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info("onCreate");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        logger.info("onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        logger.info("onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        logger.info("onTrimMemory");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        logger.info("onTerminate");
    }
}
