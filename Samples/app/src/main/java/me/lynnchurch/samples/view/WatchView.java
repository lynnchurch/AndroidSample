package me.lynnchurch.samples.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.Calendar;

import me.lynnchurch.samples.R;
import me.lynnchurch.samples.utils.Utils;

public class WatchView extends View {
    private static final String TAG = WatchView.class.getSimpleName();

    private int mHourPointerColor; // 时针颜色
    private int mMinutePointerColor; // 分针颜色
    private int mSecondPointerColor; // 秒针颜色
    private float mHourMinutePointerStrokeWidth; // 时针分针的粗细
    private int mTwelveColor; // 1 - 12 数字颜色
    private float mTwelveTextSize; // 1 - 12 数字文本大小
    private int mSixtyColor; // 05 - 60 数字颜色
    private float mSixtyTextSize; // 05 - 60 数字文本大小
    private DecimalFormat mSixtyFormat = new DecimalFormat("00"); // 05 - 60 数字格式器
    private int mScaleColor; // 刻度颜色
    private float mScaleStrokeWidth; // 刻度的粗细
    private float mScaleLength; // 刻度的长度
    private int mScreenWidth; // 屏幕宽度
    private float mPadding; // 内边距
    private int mBackground; // 表盘底色
    private float mSize; // 表盘的尺寸
    private float mCentrePointX; // 圆心点x坐标
    private float mCentrePointY; // 圆心点y坐标
    private Paint mPaint; // 画笔
    private PaintFlagsDrawFilter mPaintFlagsDrawFilter; // 画笔过滤器
    private Calendar mCalendar = Calendar.getInstance(); // 日期

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
        parseAttrs(context, attrs, defStyleAttr);
        initPaint();
    }

    private void parseAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WatchView, defStyleAttr, 0);
        mHourPointerColor = a.getColor(R.styleable.WatchView_hour_pointer_color, Color.WHITE);
        mMinutePointerColor = a.getColor(R.styleable.WatchView_minute_pointer_color, Color.WHITE);
        mSecondPointerColor = a.getColor(R.styleable.WatchView_second_pointer_color, Color.argb(222, 181, 140, 78));
        mTwelveColor = a.getColor(R.styleable.WatchView_twelve_color, Color.WHITE);
        mSixtyColor = a.getColor(R.styleable.WatchView_sixty_color, Color.WHITE);
        mScaleColor = a.getColor(R.styleable.WatchView_scale_color, Color.argb(122, 255, 255, 255));
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
        mTwelveTextSize = (w - 2 * mPadding) / 8f;
        mSixtyTextSize = (w - 2 * mPadding) / 20f;
        mScaleStrokeWidth = w / 156f;
        mScaleLength = w / 26f;
        mHourMinutePointerStrokeWidth = w / 30f;
        mCentrePointX = w / 2f;
        mCentrePointY = w / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        // 绘制表盘
        drawDial(canvas);
        // 绘制指针
        drawPointer(canvas);
    }

    /**
     * 绘制表盘
     *
     * @param canvas
     */
    private void drawDial(Canvas canvas) {
        // 绘制刻度
        drawScale(canvas);

        // 绘制外圈数字
        drawOuterNumber(canvas);

        // 绘制内圈数字
        drawInterNumber(canvas);
    }

    /**
     * 绘制刻度
     *
     * @param canvas
     */
    private void drawScale(Canvas canvas) {
        mPaint.setStrokeWidth(mScaleStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mScaleColor);
        for (int i = 60; i >= 1; i--) {
            if (i % 5 != 0) {
                canvas.drawLine(mSize / 2, mPadding, mSize / 2, mPadding + mScaleLength, mPaint);
            }
            canvas.rotate(-6, mSize / 2, mSize / 2);
        }
    }

    /**
     * 绘制外圈数字
     *
     * @param canvas
     */
    private void drawOuterNumber(Canvas canvas) {
        mPaint.setColor(mSixtyColor);
        mPaint.setTextSize(mSixtyTextSize);
        String numberText;
        Rect textBound = new Rect();
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
            // 测量文本区域
            numberText = mSixtyFormat.format(i);
            mPaint.getTextBounds(numberText, 0, numberText.length(), textBound);
            // 文字是以左下角为起点进行绘制的，为了保证文字的中心点位于圆圈上所以得进行位移
            outerX = mCentrePointX + outerRadius * (float) Math.sin(radians) - textBound.width() / 2f;
            outerY = mCentrePointY - outerRadius * (float) Math.cos(radians) + textBound.height() / 2f;
            canvas.drawText(numberText, outerX, outerY, mPaint);
        }
    }

    /**
     * 绘制内圈数字
     *
     * @param canvas
     */
    private void drawInterNumber(Canvas canvas) {
        mPaint.setColor(mTwelveColor);
        mPaint.setTextSize(mTwelveTextSize);
        String numberText;
        Rect textBound = new Rect();
        // 内圈半径
        float interRadius = mSize / 2f - mPadding - 1.6f * mScaleLength - mTwelveTextSize / 2f;
        // 内圈绘制坐标
        float interX;
        float interY;
        for (int i = 1; i <= 12; i++) {
            // 将角度转换成弧度值
            double radians = i * 30f / 360 * 2 * Math.PI;
            // 测量文本区域
            numberText = String.valueOf(i);
            mPaint.getTextBounds(numberText, 0, numberText.length(), textBound);
            // 文字是以左下角为起点进行绘制的，为了保证文字的中心点位于圆圈上所以得进行位移
            interX = mCentrePointX + interRadius * (float) Math.sin(radians) - textBound.width() / 2f;
            interY = mCentrePointY - interRadius * (float) Math.cos(radians) + textBound.height() / 2f;
            canvas.drawText(numberText, interX, interY, mPaint);
        }
    }

    /**
     * 绘制指针
     *
     * @param canvas
     */
    private void drawPointer(Canvas canvas) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // 绘制时针
        drawHourPointer(canvas);
        // 绘制分针
        drawMinutePointer(canvas);
        // 绘制秒针
        drawSecondPointer(canvas);

        // 每隔1秒刷新一次
        postInvalidateDelayed(1000);
    }

    /**
     * 绘制时针
     *
     * @param canvas
     */
    private void drawHourPointer(Canvas canvas) {
        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);
        mPaint.setColor(mHourPointerColor);
        mPaint.setStrokeWidth(mHourMinutePointerStrokeWidth / 2.6f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 时针转过的角度
        float hourAngle = (hour + minute / 60f + second / 3600f) * 360 / 12f;
        double hourRadians = hourAngle / 360 * 2 * Math.PI;
        // 绘制指针细的一端
        float thinPointerLength = mSize / 15f;
        float thinPointerX = mCentrePointX + thinPointerLength * (float) Math.sin(hourRadians);
        float thinPointerY = mCentrePointY - thinPointerLength * (float) Math.cos(hourRadians);
        canvas.drawLine(mCentrePointX, mCentrePointY, thinPointerX, thinPointerY, mPaint);
        // 绘制指针粗的一端
        float thickPointerLength = mSize / 5f;
        float thickPointerX = mCentrePointX + thickPointerLength * (float) Math.sin(hourRadians);
        float thickPointerY = mCentrePointY - thickPointerLength * (float) Math.cos(hourRadians);
        mPaint.setStrokeWidth(mHourMinutePointerStrokeWidth);
        canvas.drawLine(thinPointerX, thinPointerY, thickPointerX, thickPointerY, mPaint);
        // 绘制圆心点
        canvas.drawCircle(mCentrePointX, mCentrePointY, mHourMinutePointerStrokeWidth / 2, mPaint);
    }

    /**
     * 绘制分针
     *
     * @param canvas
     */
    private void drawMinutePointer(Canvas canvas) {
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);
        mPaint.setColor(mMinutePointerColor);
        mPaint.setStrokeWidth(mHourMinutePointerStrokeWidth / 2.6f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 分针转过的角度
        float minuteAngle = (minute + second / 60f) * 360 / 60f;
        double minuteRadians = minuteAngle / 360 * 2 * Math.PI;
        // 绘制指针细的一端
        float thinPointerLength = mSize / 15f;
        float thinPointerX = mCentrePointX + thinPointerLength * (float) Math.sin(minuteRadians);
        float thinPointerY = mCentrePointY - thinPointerLength * (float) Math.cos(minuteRadians);
        canvas.drawLine(mCentrePointX, mCentrePointY, thinPointerX, thinPointerY, mPaint);
        // 绘制指针粗的一端
        float thickPointerLength = mSize / 2.85f;
        float thickPointerX = mCentrePointX + thickPointerLength * (float) Math.sin(minuteRadians);
        float thickPointerY = mCentrePointY - thickPointerLength * (float) Math.cos(minuteRadians);
        mPaint.setStrokeWidth(mHourMinutePointerStrokeWidth);
        canvas.drawLine(thinPointerX, thinPointerY, thickPointerX, thickPointerY, mPaint);
        // 绘制圆心点
        canvas.drawCircle(mCentrePointX, mCentrePointY, mHourMinutePointerStrokeWidth / 2, mPaint);
    }

    /**
     * 绘制秒针
     *
     * @param canvas
     */
    private void drawSecondPointer(Canvas canvas) {
        int second = mCalendar.get(Calendar.SECOND);
        mPaint.setColor(mSecondPointerColor);
        mPaint.setStrokeWidth(mHourMinutePointerStrokeWidth / 4f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 秒针转过的角度
        float secondAngle = second * 360 / 60f;
        double secondRadians = secondAngle / 360 * 2 * Math.PI;
        // 绘制长指针的一端
        float longPointerLength = mSize / 2.5f;
        float longPointerX = mCentrePointX + longPointerLength * (float) Math.sin(secondRadians);
        float longPointerY = mCentrePointY - longPointerLength * (float) Math.cos(secondRadians);
        canvas.drawLine(mCentrePointX, mCentrePointY, longPointerX, longPointerY, mPaint);
        // 绘制短指针的一端
        float shortPointerLength = mSize / 14.5f;
        float thickPointerX = mCentrePointX + shortPointerLength * (float) Math.sin(secondRadians + Math.PI);
        float thickPointerY = mCentrePointY - shortPointerLength * (float) Math.cos(secondRadians + Math.PI);
        canvas.drawLine(mCentrePointX, mCentrePointY, thickPointerX, thickPointerY, mPaint);
        // 绘制圆心点
        mPaint.setColor(Color.BLACK);
        canvas.drawCircle(mCentrePointX, mCentrePointY, mHourMinutePointerStrokeWidth / 5, mPaint);
        mPaint.setColor(mSecondPointerColor);
        canvas.drawCircle(mCentrePointX, mCentrePointY, mHourMinutePointerStrokeWidth / 3, mPaint);
    }
}
