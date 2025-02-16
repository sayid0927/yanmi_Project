package com.zxly.o2o.view;

/**
 * Created by dsnx on 2016/6/14.
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.zxly.o2o.adapter.DataSetAdapter;
import com.zxly.o2o.shop.R;

public class VerticalRollingTextView extends TextView {

    DataSetAdapter mDataSetAdapter;

    private final Paint mPaint;

    private int mCurrentIndex;
    private int mNextIndex;

    Rect bounds = new Rect();

    private float mCurrentOffsetY;

    private float mOrgOffsetY = -1;

    private final float mTextTopToAscentOffset;
    private float mOffset;

    private InternalAnimation mAnimation = new InternalAnimation();

    /*防止动画结束的回调触发以后动画继续进行出现的错乱问题*/
    private boolean mAnimationEnded;

    private boolean isRunning;
    /*动画时间*/
    private int mDuration = 1000;
    /*动画间隔*/
    private int mAnimInterval = 2000;

    private int mColor, mHighlightColor;

    public VerticalRollingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setTypeface(Typeface.DEFAULT);

        parseAttrs(context, attrs);

        Paint.FontMetricsInt metricsInt = mPaint.getFontMetricsInt();
        mTextTopToAscentOffset = metricsInt.ascent - metricsInt.top;

        mAnimation.setDuration(mDuration);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        float density = getResources().getDisplayMetrics().density;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalRollingTextView);
        mColor = typedArray.getColor(R.styleable.VerticalRollingTextView_vrColor, Color.BLACK);
        mHighlightColor = typedArray.getColor(R.styleable.VerticalRollingTextView_vrHighlightColor, Color.BLACK);
        mPaint.setColor(typedArray.getColor(R.styleable.VerticalRollingTextView_vrColor, Color.BLACK));
        mPaint.setTextSize(typedArray.getDimensionPixelOffset(R.styleable.VerticalRollingTextView_vrSize, (int) (density * 14)));
        mDuration = typedArray.getInt(R.styleable.VerticalRollingTextView_duration, mDuration);
        mAnimInterval = typedArray.getInt(R.styleable.VerticalRollingTextView_animInterval, mAnimInterval);

        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制文本
        if (mDataSetAdapter == null || mDataSetAdapter.isEmpty()) {
            super.onDraw(canvas);
            return;
        }
        String text1 = mDataSetAdapter.getText(mCurrentIndex);
        String text2 = mDataSetAdapter.getText(mNextIndex);
        String[] previous=text1.split("#");
        String[] next=text2.split("#");
        int previousX = 0;
        int nextX = 0;
        //只需要进行一次测量
        if (mOrgOffsetY == -1) {
            mPaint.getTextBounds(text1, 0, text1.length(), bounds);
            mOffset = (getHeight() + bounds.height()) * 0.5f;
            mOrgOffsetY = mCurrentOffsetY = mOffset - mTextTopToAscentOffset;
            mAnimation.updateValue(mOrgOffsetY, -2 * mTextTopToAscentOffset);
        }

        mPaint.getTextBounds(previous[0], 0, previous[0].length(), bounds);
        previousX = previousX + bounds.width() + 10;
        mPaint.setColor(mColor);
        canvas.drawText(previous[0], 0, mCurrentOffsetY, mPaint);
        for (int i = 1; i < previous.length; i++) {
            mPaint.setColor(i % 2 == 0 ? mColor : mHighlightColor);
            canvas.drawText(previous[i], previousX, mCurrentOffsetY, mPaint);
            mPaint.getTextBounds(previous[i], 0, previous[i].length(), bounds);
            previousX = previousX + bounds.width() + 10;
        }

        mPaint.getTextBounds(next[0], 0, next[0].length(), bounds);
        nextX = nextX + bounds.width() + 10;
        mPaint.setColor(mColor);
        canvas.drawText(next[0], 0, mCurrentOffsetY + mOffset + mTextTopToAscentOffset, mPaint);
        for (int i = 1; i < next.length; i++) {
            mPaint.setColor(i % 2 == 0 ? mColor : mHighlightColor);
            canvas.drawText(next[i], nextX, mCurrentOffsetY + mOffset + mTextTopToAscentOffset, mPaint);
            mPaint.getTextBounds(next[i], 0, next[i].length(), bounds);
            nextX = nextX + bounds.width() + 10;
        }


/*

        mPaint.getTextBounds(previous[0],0,previous[0].length(),bounds);
//        mPaint.setColor(Color.parseColor("#333333"));
        mPaint.setColor(mColor);
        canvas.drawText(previous[0], 0, mCurrentOffsetY, mPaint);
        mPaint.setColor(Color.parseColor("#ff5f19"));
        canvas.drawText("  "+previous[1], bounds.width(), mCurrentOffsetY, mPaint);

        mPaint.getTextBounds(next[0],0,next[0].length(),bounds);
//        mPaint.setColor(Color.parseColor("#333333"));
        mPaint.setColor(mColor);
        canvas.drawText(next[0], 0, mCurrentOffsetY + mOffset + mTextTopToAscentOffset, mPaint);
        mPaint.setColor(Color.parseColor("#ff5f19"));
        canvas.drawText("  "+next[1], bounds.width(), mCurrentOffsetY + mOffset + mTextTopToAscentOffset, mPaint);
        /*canvas.drawText(text1, 0, mCurrentOffsetY, mPaint);
        canvas.drawText(text2, 0, mCurrentOffsetY + mOffset + mTextTopToAscentOffset, mPaint);*/




    }

    public void setDataSetAdapter(DataSetAdapter dataSetAdapter) {
        mDataSetAdapter = dataSetAdapter;
        confirmNextIndex();
        invalidate();
    }

    /**
     * 开始转动,界面可见的时候调用
     */
    public void run() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        mAnimation.updateValue(mCurrentOffsetY, -2 * mTextTopToAscentOffset);
        post(mRollingTask);
    }

    /**
     * @return true代表正在转动
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 停止转动,界面不可见的时候调用
     */
    public void stop() {
        isRunning = false;
        removeCallbacks(mRollingTask);
    }

    Runnable mRollingTask = new Runnable() {
        @Override
        public void run() {
            mAnimationEnded = false;
            startAnimation(mAnimation);
            postDelayed(this, mAnimInterval);
        }
    };


    public void animationEnd() {
        //1.角标+1
        mCurrentIndex++;
        //2.计算出正确的角标
        mCurrentIndex = mCurrentIndex < mDataSetAdapter.getItemCount() ? mCurrentIndex : mCurrentIndex % mDataSetAdapter.getItemCount();
        //3.计算下一个待显示文字角标
        confirmNextIndex();
        //3.位置复位
        mCurrentOffsetY = mOrgOffsetY;
        mAnimationEnded = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mRollingTask);
        if (isRunning()) {
            mAnimation.cancel();
        }
    }

    /**
     * 计算第二个角标
     */
    private void confirmNextIndex() {
        //3.计算第二个角标
        mNextIndex = mCurrentIndex + 1;
        //4.计算出正确的第二个角标
        mNextIndex = mNextIndex < mDataSetAdapter.getItemCount() ? mNextIndex : 0;
    }

    /**
     * float估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    float evaluate(float fraction, float startValue, float endValue) {
        return startValue + fraction * (endValue - startValue);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {

    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(VerticalRollingTextView.this, mCurrentIndex);
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(VerticalRollingTextView view, int index);
    }

    class InternalAnimation extends Animation {

        float startValue;
        float endValue;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (mAnimationEnded) return;

            mCurrentOffsetY = evaluate(interpolatedTime, startValue, endValue);
            if (mCurrentOffsetY == endValue) {
                animationEnd();
            }
            postInvalidate();
        }

        public void updateValue(float startValue, float endValue) {
            this.startValue = startValue;
            this.endValue = endValue;
        }

    }

}
