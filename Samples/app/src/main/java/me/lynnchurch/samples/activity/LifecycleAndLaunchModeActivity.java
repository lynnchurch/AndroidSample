package me.lynnchurch.samples.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.jakewharton.rxbinding3.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;

import me.lynnchurch.samples.R;
import me.lynnchurch.samples.utils.Utils;

public class LifecycleAndLaunchModeActivity extends BaseActivity {
    private static final int REQUEST_CODE_AACTIVITY = 1000;
    private static final String TAG = LifecycleAndLaunchModeActivity.class.getSimpleName();
    private final RxPermissions rxPermissions = new RxPermissions(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[0]);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_lifecycle_and_launch_mode;
    }

    protected void init() {
        RxView.clicks(findViewById(R.id.btnLaunchImplicit))
                .compose(rxPermissions.ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(granted -> {
                    if (granted) {
                        Intent intent = new Intent();
                        intent.setAction("lynnchurch.intent.action.FIRST");
                        intent.addCategory("lynnchurch.intent.category.OK");
                        // 这里的路径Environment.getExternalStorageDirectory()和my_files须和files_paths.xml文件中的external-path和path保持一致
                        File dir = new File(Environment.getExternalStorageDirectory() + "/my_files");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File imageFile = new File(dir, "temp.png");
                        if (!imageFile.exists()) {
                            try {
                                imageFile.createNewFile();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }
                        Uri imageUri = FileProvider.getUriForFile(this,
                                getPackageName() + ".fileprovider",
                                imageFile);

                        intent.setDataAndType(imageUri, "image/png");
                        if (!Utils.isAppInstalled(this, intent)) {
                            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
                            return;
                        }
                        intent.putExtra("msg", "Hello, msg is from Samples.");
                        startActivity(intent);
                    } else {
                        toast("没有存储权限，你不可以这样", Toast.LENGTH_SHORT);
                    }

                });
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
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

    public void toAssistAActivityExplicit(View view) {
        if (!Utils.isAppInstalled(this, "me.lynnchurch.assist")) {
            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("msg", "Hello, msg is from Samples.");
        intent.setClassName("me.lynnchurch.assist", "me.lynnchurch.assist.activity.AActivity");
        startActivityForResult(intent, REQUEST_CODE_AACTIVITY);
    }

    public void toAssistAActivityExplicitWithNewTask(View view) {
        if (!Utils.isAppInstalled(this, "me.lynnchurch.assist")) {
            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent();
        intent.setClassName("me.lynnchurch.assist", "me.lynnchurch.assist.activity.AActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void toAssistCActivityExplicit(View view) {
        if (!Utils.isAppInstalled(this, "me.lynnchurch.assist")) {
            toast(R.string.app_not_installed, Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent();
        intent.setClassName("me.lynnchurch.assist", "me.lynnchurch.assist.activity.CActivity");
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
