package me.lynnchurch.samples.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import me.lynnchurch.samples.R;

public class DragViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.view_samples)[0]);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_drag_view;
    }
}
