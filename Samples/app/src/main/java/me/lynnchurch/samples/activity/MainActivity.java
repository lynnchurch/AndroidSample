package me.lynnchurch.samples.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.TitlesAdapter;

public class MainActivity extends BaseActivity {
    private static final int POSITION_LIFECYCLE_AND_LAUNCH_MODE = 0;
    private static final int POSITION_IPC = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView rvTitles;
    private TitlesAdapter titlesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init() {
        rvTitles = findViewById(R.id.rvTitles);
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
                        intent = new Intent(MainActivity.this, IPCActivity.class);
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
