package me.lynnchurch.samples.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.logging.Logger;

import me.lynnchurch.samples.R;
import me.lynnchurch.samples.utils.Utils;

public class LifecycleAndLaunchModeActivity extends BaseActivity {
    private static final int REQUEST_CODE_AACTIVITY = 1000;
    private final Logger logger = Logger.getLogger(LifecycleAndLaunchModeActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");
        setContentView(R.layout.activity_lifecycle_and_launch_mode);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[0]);
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

    public void toEActivity(View view) {
        startActivity(new Intent(this, EActivity.class));
    }

    public void toTargetAActivityExplicit(View view) {
        if (!Utils.isAppInstalled(this, "me.lynnchurch.target")) {
            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("msg", "Hello, msg is from Samples.");
        intent.setClassName("me.lynnchurch.target", "me.lynnchurch.target.activity.AActivity");
        startActivityForResult(intent, REQUEST_CODE_AACTIVITY);
    }

    public void toTargetAActivityExplicitWithNewTask(View view) {
        if (!Utils.isAppInstalled(this, "me.lynnchurch.target")) {
            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent();
        intent.setClassName("me.lynnchurch.target", "me.lynnchurch.target.activity.AActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void toTargetActivityImplicit(View view) {
        Intent intent = new Intent();
        intent.setAction("lynnchurch.intent.action.FIRST");
        intent.addCategory("lynnchurch.intent.category.OK");
        intent.setDataAndType(Uri.parse("file://aaa"), "image/png");
        if (!Utils.isAppInstalled(this, intent)) {
            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
            return;
        }
        intent.putExtra("msg", "Hello, msg is from Samples.");
        startActivity(intent);
    }

    public void toTargetCActivityExplicit(View view) {
        if (!Utils.isAppInstalled(this, "me.lynnchurch.target")) {
            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent();
        intent.setClassName("me.lynnchurch.target", "me.lynnchurch.target.activity.CActivity");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_AACTIVITY:
                String msg = data.getStringExtra("msg");
                if (null != msg) {
                    toast("Got msg：" + msg, Toast.LENGTH_LONG);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
