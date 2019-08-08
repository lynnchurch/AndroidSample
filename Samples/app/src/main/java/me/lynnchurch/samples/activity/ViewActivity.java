package me.lynnchurch.samples.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.adapter.StringArrayAdapter;

public class ViewActivity extends BaseActivity {
    @BindView(R.id.rvSamples)
    RecyclerView rvSamples;
    private String[] mViewSamples;
    private StringArrayAdapter mStringArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.titles)[4]);
        init();
    }

    private void init() {
        rvSamples.setLayoutManager(new LinearLayoutManager(this));
        mViewSamples = getResources().getStringArray(R.array.view_samples);
        mStringArrayAdapter = new StringArrayAdapter(mViewSamples);
        mStringArrayAdapter.setOnItemClickListener((v, position) -> {
            String itemText = v.getText().toString();
            if (mViewSamples[0].equals(itemText)) {
                startActivity(new Intent(ViewActivity.this, DragViewActivity.class));
                return;
            }
        });
        rvSamples.setAdapter(mStringArrayAdapter);
        rvSamples.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_view;
    }
}
