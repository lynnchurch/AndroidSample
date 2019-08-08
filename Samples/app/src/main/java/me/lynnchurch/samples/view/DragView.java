package me.lynnchurch.samples.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DragView extends View {
    private static final String TAG = DragView.class.getSimpleName();
    private float mLastX;
    private float mLastY;
    private Scroller mScroller;

    public DragView(Context context) {
        this(context, null, 0);
    }

    public DragView(Context context, @NonNull AttributeSet atts) {
        this(context, atts, 0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
    }

    public void smoothScrollBy(int dx, int dy) {
        int startX = (int) getX();
        int startY = (int) getY();
        mScroller.startScroll(startX, startY, dx, dy, 666);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            setX(mScroller.getCurrX());
            setY(mScroller.getCurrY());
            postInvalidate();
        }
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
