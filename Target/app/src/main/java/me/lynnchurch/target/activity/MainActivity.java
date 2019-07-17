package me.lynnchurch.target.activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.logging.Logger;

import me.lynnchurch.target.R;

public class MainActivity extends BaseActivity {
    private final Logger logger = Logger.getLogger(MainActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logger.info("onRestoreInstanceState");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logger.info("onNewIntent");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        logger.info("onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        logger.info("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.info("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.info("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        logger.info("onStop");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        logger.info("onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.info("onDestroy");
    }

    public void toAActivity(View view) {
        startActivity(new Intent(this, AActivity.class));
    }

    public void toBActivity(View view) {
        startActivity(new Intent(this, BActivity.class));
    }

    public void toCActivity(View view) {
        startActivity(new Intent(this, CActivity.class));
    }

    public void toDActivity(View view) {
        startActivity(new Intent(this, DActivity.class));
    }
}
