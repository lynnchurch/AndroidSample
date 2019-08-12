package me.lynnchurch.samples.activity;

import android.os.Bundle;

import me.lynnchurch.samples.R;

public class WatchViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(WatchViewActivity.class.getSimpleName());
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_watch_view;
    }
}
