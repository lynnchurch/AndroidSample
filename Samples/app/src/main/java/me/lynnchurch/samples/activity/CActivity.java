package me.lynnchurch.samples.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.logging.Logger;

import me.lynnchurch.samples.R;

public class CActivity extends BaseActivity {
    private Logger logger = Logger.getLogger(CActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");
        setContentView(R.layout.activity_c);
        getSupportActionBar().setTitle("CActivity");
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
        Intent intent = new Intent(this, AActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void toBActivity(View view) {
        startActivity(new Intent(this, BActivity.class));
    }
}
