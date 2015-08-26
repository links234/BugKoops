package com.intel.bugkoops.UI;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.Button;
import com.intel.bugkoops.R;

public class ScanButton extends Button {

    private static final float INNER_RADIUS_PERCENTAGE = 0.75f;

    private static final int PRESSED_COLOR_LIGHTUP = 255 / 25;
    private static final int PRESSED_RING_ALPHA = 75;
    private static final int NORMAL_ANIMATION_TIME = 1200;

    private int mCenterY;
    private int mCenterX;
    private int mOuterRadius;
    private int mInnerRadius;

    private Paint mInnerPaint;
    private Paint mOuterPaint;

    private float animationProgress;

    private int mDefaultColor = Color.BLACK;
    private int mPressedColor;
    private ObjectAnimator mPressedAnimator;

    private Handler mHandler;

    public ScanButton(Context context) {
        super(context);
        init(context, null);
    }

    public ScanButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScanButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);

        if (mInnerPaint != null) {
            mInnerPaint.setColor(pressed ? mPressedColor : mDefaultColor);
        }

        if (pressed) {
            disableRing();
        } else {
            enableRing();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius + animationProgress, mOuterPaint);
        canvas.drawCircle(mCenterX, mCenterY, mInnerRadius, mInnerPaint);
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mCenterX = width / 2;
        mCenterY = height / 2;
        mOuterRadius = Math.min(width, height) / 2;
        mInnerRadius = (int)((float)mOuterRadius*INNER_RADIUS_PERCENTAGE);
        enableRing();
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
        this.invalidate();
    }

    public void setColor(int color) {
        this.mDefaultColor = color;
        this.mPressedColor = getHighlightColor(color, PRESSED_COLOR_LIGHTUP);

        mInnerPaint.setColor(mDefaultColor);
        mOuterPaint.setColor(mDefaultColor);
        mOuterPaint.setAlpha(PRESSED_RING_ALPHA);
    }

    private void disableRing() {
        mHandler.removeCallbacksAndMessages(null);
        mPressedAnimator.setFloatValues(animationProgress, mOuterRadius - mInnerRadius);
        mPressedAnimator.setRepeatCount(0);
        mPressedAnimator.setDuration((long) ((mOuterRadius - mInnerRadius - animationProgress) / (mOuterRadius - mInnerRadius) * NORMAL_ANIMATION_TIME));
        mPressedAnimator.start();
    }

    private void enableRing() {
        mHandler.removeCallbacksAndMessages(null);
        long goBackTime = (long)((animationProgress)/(mOuterRadius-mInnerRadius)*NORMAL_ANIMATION_TIME);

        mPressedAnimator.setFloatValues(animationProgress, 0.0f);
        mPressedAnimator.setRepeatCount(0);
        mPressedAnimator.setDuration(goBackTime);
        mPressedAnimator.start();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPressedAnimator.setFloatValues(0.0f, mOuterRadius - mInnerRadius);
                mPressedAnimator.setDuration(NORMAL_ANIMATION_TIME);
                mPressedAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                mPressedAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                mPressedAnimator.start();
            }
        }, goBackTime);
    }

    public void reset() {
        mHandler.removeCallbacksAndMessages(null);
        mPressedAnimator.setFloatValues(0.0f, mOuterRadius - mInnerRadius);
        mPressedAnimator.setDuration(NORMAL_ANIMATION_TIME);
        mPressedAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        mPressedAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mPressedAnimator.start();
    }

    private void init(Context context, AttributeSet attrs) {
        this.setFocusable(true);
        setClickable(true);

        mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerPaint.setStyle(Paint.Style.FILL);

        mOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterPaint.setStyle(Paint.Style.FILL);

        int color = context.getResources().getColor(R.color.bugkoops_scan);

        setColor(color);

        mHandler = new Handler();

        animationProgress = 0.0f;
        mPressedAnimator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 0f);
        mPressedAnimator.setDuration(NORMAL_ANIMATION_TIME);
        enableRing();
    }

    private int getHighlightColor(int color, int amount) {
        return Color.argb(Math.min(255, Color.alpha(color)), Math.min(255, Color.red(color) + amount),
                Math.min(255, Color.green(color) + amount), Math.min(255, Color.blue(color) + amount));
    }
}
