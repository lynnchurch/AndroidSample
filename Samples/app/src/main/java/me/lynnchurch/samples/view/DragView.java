package me.lynnchurch.samples.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DragView extends View {
    private static final String TAG = DragView.class.getSimpleName();
    private float mLastX;
    private float mLastY;

    public DragView(Context context) {
        this(context, null, 0);
    }

    public DragView(Context context, @NonNull AttributeSet atts) {
        this(context, atts, 0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        float deltaX = x - mLastX;
        float deltaY = y - mLastY;
        Log.i(TAG, "x:" + x + " y:" + y + " deltaX:" + deltaX + " deltaY:" + deltaY);
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            setTranslationX(getTranslationX() + deltaX);
            setTranslationY(getTranslationY() + deltaY);
        }
        mLastX = x;
        mLastY = y;
        return true;
    }
}
