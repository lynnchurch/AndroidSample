package me.lynnchurch.samples.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {
    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutResID = getLayoutResID();
        setContentView(layoutResID);
        mUnbinder = ButterKnife.bind(this);
    }

    protected abstract int getLayoutResID();

    public void toast(int strResId, int duration) {
        Toast.makeText(this, strResId, duration).show();
    }

    public void toast(String str, int duration) {
        Toast.makeText(this, str, duration).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
