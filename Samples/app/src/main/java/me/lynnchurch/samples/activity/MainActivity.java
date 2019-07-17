package me.lynnchurch.samples.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.logging.Logger;

import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.TitlesAdapter;

public class MainActivity extends BaseActivity {
    private static final int POSITION_LIFECYCLE_AND_LAUNCH_MODE = 0;
    private Logger logger = Logger.getLogger(MainActivity.class.getSimpleName());
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
                logger.info("click:" + position);
                Intent intent;
                switch (position) {
                    case POSITION_LIFECYCLE_AND_LAUNCH_MODE:
                        intent = new Intent(MainActivity.this, LifecycleAndLaunchModeActivity.class);
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
}
