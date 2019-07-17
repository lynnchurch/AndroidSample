package me.lynnchurch.target.activity;

import androidx.annotation.NonNull;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.util.logging.Logger;

import me.lynnchurch.target.R;


public class AActivity extends BaseActivity {
    Logger logger = Logger.getLogger(AActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");
        setContentView(R.layout.activity_a);
        getSupportActionBar().setTitle("Target AActivity");

        Intent intent = getIntent();
        String msg = intent.getStringExtra("msg");
        if (null != msg) {
            toast("Got msg：" + msg, Toast.LENGTH_LONG);
        }
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra("msg", "Hello, msg is from Target");
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
