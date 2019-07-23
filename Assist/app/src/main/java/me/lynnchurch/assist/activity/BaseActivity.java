package me.lynnchurch.assist.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void toast(int strResId, int duration) {
        Toast.makeText(this, strResId, duration).show();
    }

    public void toast(String str, int duration) {
        Toast.makeText(this, str, duration).show();
    }
}
