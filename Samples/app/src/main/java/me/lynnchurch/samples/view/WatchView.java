package me.lynnchurch.samples.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

import me.lynnchurch.samples.R;
import me.lynnchurch.samples.utils.Utils;

public class WatchView extends View {
    private static final String TAG = WatchView.class.getSimpleName();

    private int mHourPointerColor; // 时针颜色
    private int mMinutePointerColor; // 分针颜色
    private int mSecondPointerColor; // 秒针颜色
    private int mTwelveColor; // 1 - 12 数字颜色
    private float mTwelveTextSize; // 1 - 12 数字文本大小
    private int mSixtyColor; // 05 - 60 数字颜色
    private float mSixtyTextSize; // 05 - 60 数字文本大小
    private DecimalFormat mSixtyFormat = new DecimalFormat("00"); // 05 - 60 数字格式器
    private int mScaleColor; // 刻度颜色
    private float mScaleStrokeWidth; // 刻度的粗细
    private float mScaleLength; // 刻度的长度
    private int mScreenWidth; // 屏幕宽度
    private int mScreenHeight; // 屏幕高度
    private float mPadding; // 内边距
    private int mBackground; // 表盘底色
    private float mSize; // 表盘的尺寸
    private Paint mPaint; // 画笔
    private PaintFlagsDrawFilter mPaintFlagsDrawFilter; // 画笔过滤器

    public WatchView(Context context) {
        this(context, null, 0);
    }

    public WatchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        int heightPixels = context.getResources().getDisplayMetrics().heightPixels;
        mScreenWidth = Math.min(widthPixels, heightPixels);
        mScreenHeight = Math.max(widthPixels, heightPixels);
        parseAttrs(context, attrs, defStyleAttr);
        initPaint();
    }

    private void parseAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WatchView, defStyleAttr, 0);
        mHourPointerColor = a.getColor(R.styleable.WatchView_hour_pointer_color, Color.WHITE);
        mMinutePointerColor = a.getColor(R.styleable.WatchView_minute_pointer_color, Color.WHITE);
        mSecondPointerColor = a.getColor(R.styleable.WatchView_second_pointer_color, Color.argb(200, 181, 140, 78));
        mTwelveColor = a.getColor(R.styleable.WatchView_twelve_color, Color.WHITE);
        mSixtyColor = a.getColor(R.styleable.WatchView_sixty_color, Color.WHITE);
        mScaleColor = a.getColor(R.styleable.WatchView_scale_color, Color.argb(200, 255, 255, 255));
        mPadding = a.getDimension(R.styleable.WatchView_android_padding, Utils.dip2px(context, 16));
        mBackground = a.getColor(R.styleable.WatchView_android_background, Color.BLACK);
        setBackgroundColor(mBackground);
        a.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (size > 0) {
            size = Math.min(size, mScreenWidth);
        } else {
            size = mScreenWidth;
        }
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSize = w;
        mTwelveTextSize = (w - 2 * mPadding) / 9;
        mSixtyTextSize = (w - 2 * mPadding) / 20;
        mScaleStrokeWidth = w / 125;
        mScaleLength = w / 24;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDial(canvas);
    }

    /**
     * 绘制表盘
     *
     * @param canvas
     */
    private void drawDial(Canvas canvas) {
        // 绘制刻度
        mPaint.setStrokeWidth(mScaleStrokeWidth);
        mPaint.setColor(mScaleColor);
        for (int i = 60; i >= 1; i--) {
            if (i % 5 != 0) {
                canvas.drawLine(mSize / 2, mPadding, mSize / 2, mPadding + mScaleLength, mPaint);
            }
            canvas.rotate(-6, mSize / 2, mSize / 2);
        }

        // 圆点坐标
        float x0 = mSize / 2;
        float y0 = mSize / 2;

        // 绘制外圈数字
        mPaint.setColor(mSixtyColor);
        mPaint.setTextSize(mSixtyTextSize);
        String text = mSixtyFormat.format(1);
        Rect textBound = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), textBound);
        // 外圈半径
        float outerRadius = mSize / 2f - mPadding - mScaleLength / 2f;
        // 外圈绘制坐标
        float outerX;
        float outerY;
        for (int i = 5; i <= 60; i++) {
            if (i % 5 != 0) {
                continue;
            }
            // 将角度转换成弧度值
            double radians = i * 6f / 360 * 2 * Math.PI;
            outerX = x0 + outerRadius * (float) Math.sin(radians) - textBound.width() / 2f;
            outerY = y0 - outerRadius * (float) Math.cos(radians) + textBound.height() / 2f;
            text = mSixtyFormat.format(i);
            mPaint.getTextBounds(text, 0, text.length(), textBound);
            canvas.drawText(text, outerX, outerY, mPaint);
        }
    }
}
