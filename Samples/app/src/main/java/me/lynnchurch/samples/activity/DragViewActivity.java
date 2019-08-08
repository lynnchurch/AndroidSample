package me.lynnchurch.samples.activity;

import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import me.lynnchurch.samples.R;
import me.lynnchurch.samples.view.DragView;

public class DragViewActivity extends BaseActivity {
    @BindView(R.id.dragView)
    DragView dragView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.view_samples)[0]);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_drag_view;
    }

    public void left(View v) {
        dragView.smoothScrollBy(-100, 0);
    }

    public void right(View v) {
        dragView.smoothScrollBy(100, 0);
    }

    public void up(View v) {
        dragView.smoothScrollBy(0, -100);
    }

    public void down(View v) {
        dragView.smoothScrollBy(0, 100);
    }
}
