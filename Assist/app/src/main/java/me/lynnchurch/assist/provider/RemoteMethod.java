package me.lynnchurch.assist.provider;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface RemoteMethod {
    String getMethodName();

    Bundle invoke(@Nullable String arg, @Nullable Bundle extras);
}
