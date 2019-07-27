package me.lynnchurch.samples.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class Utils {
    private Utils() {
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isAppInstalled(Context context, Intent intent) {
        return (intent.resolveActivity(context.getPackageManager()) != null);
    }

}
