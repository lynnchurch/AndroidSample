package me.lynnchurch.samples.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.TitlesAdapter;

public class MainActivity extends BaseActivity {
    private static final int POSITION_LIFECYCLE_AND_LAUNCH_MODE = 0;
    private static final int POSITION_IPC = 1;
    private static final int POSITION_CONTENT_PROVIDER = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final RxPermissions rxPermissions = new RxPermissions(this);
    @BindView(R.id.rvTitles)
    RecyclerView rvTitles;
    private TitlesAdapter titlesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    protected void init() {
        requestPermissions();
        titlesAdapter = new TitlesAdapter(getResources().getStringArray(R.array.titles));
        titlesAdapter.setOnItemClickListener(new TitlesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.i(TAG, "click:" + position);
                Intent intent;
                switch (position) {
                    case POSITION_LIFECYCLE_AND_LAUNCH_MODE:
                        intent = new Intent(MainActivity.this, LifecycleAndLaunchModeActivity.class);
                        break;
                    case POSITION_IPC:
                        intent = new Intent(MainActivity.this, AIDLActivity.class);
                        break;
                    case POSITION_CONTENT_PROVIDER:
                        intent = new Intent(MainActivity.this, ContentProviderActivity.class);
                        break;
                    default:
                        intent = new Intent(MainActivity.this, LifecycleAndLaunchModeActivity.class);
                }
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
        rvTitles.setLayoutManager(new LinearLayoutManager(this));
        rvTitles.setAdapter(titlesAdapter);
        rvTitles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void requestPermissions() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        Log.i(TAG, "WRITE_EXTERNAL_STORAGE is granted");
                    } else {
                        Log.i(TAG, "WRITE_EXTERNAL_STORAGE is not granted");
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

}
