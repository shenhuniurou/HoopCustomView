package com.xx.hoopcustomview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wxx
 * on 2016/11/11.
 */

public class HoopView extends View {

    private static final String TAG = HoopView.class.getSimpleName();

    private int mThemeColor = Color.YELLOW;//按钮颜色和菜单按钮的颜色
    private int mTextColor = Color.WHITE;//按钮内文字的颜色
    private int mDirection = 1;//菜单弹出的方向
    private String mText;//按钮内文字
    private String mCount;//目前送的金币数量
    private Paint mPopPaint;//弹出框背景画笔
    private Paint mBgPaint;//大圆画笔
    private TextPaint mTextPaint;
    private TextPaint mCountTextPaint;
    private PointF circle;//大圆的圆心
    private float mBigRadius = 70;//大圆的半径
    private float mSmallRadius = 50;//小圆的半径

    private int mInitNum = 1;//最小的数字，默认是1
    private int mMultiple = 5;//倍数，默认5倍
    private int mButtonCount = 3;//按钮的个数, 默认3个

    //三个按钮的圆心
    private PointF circleOne, circleTwo, circleThree;

    private int mState = STATE_NORMAL;//当前展开收缩的状态
    private boolean mIsRun = false;//是否正在展开或收缩

    //正常状态
    public static final int STATE_NORMAL = 0;
    //按钮展开
    public static final int STATE_EXPAND = 1;
    //按钮收缩
    public static final int STATE_SHRINK = 2;

    /*----------view的宽高-----------*/
    private int mWidth;
    private int mHeight = 200;

    private int mChangeWidth;//背景框改变的长度
    private int mChange;//背景框当前改变的值

    OnClickButtonListener listener;

    public HoopView(Context context) {
        this(context, null);
    }


    public HoopView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }


    public HoopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HoopView);
        mThemeColor = typedArray.getColor(R.styleable.HoopView_theme_color, Color.YELLOW);
        mDirection = typedArray.getInt(R.styleable.HoopView_direction, 1);
        mText = typedArray.getString(R.styleable.HoopView_text);
        mCount = typedArray.getString(R.styleable.HoopView_count);
        mInitNum = typedArray.getInt(R.styleable.HoopView_init_number, 1);
        mMultiple = typedArray.getInt(R.styleable.HoopView_multiple, 5);
        mButtonCount = typedArray.getInt(R.styleable.HoopView_button_count, 3);

        //根据按钮个数计算背景框改变的长度
        mChangeWidth = (int) (2 * mSmallRadius * 3 + (mButtonCount + 1) * 10);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(mThemeColor);
        mBgPaint.setAlpha(190);
        mBgPaint.setStyle(Paint.Style.FILL);

        mPopPaint = new Paint();
        mPopPaint.setAntiAlias(true);
        mPopPaint.setColor(Color.GRAY);
        mPopPaint.setAlpha(190);
        mPopPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(context.getResources().getDimension(R.dimen.hoop_text_size));

        mCountTextPaint = new TextPaint();
        mCountTextPaint.setAntiAlias(true);
        mCountTextPaint.setColor(mThemeColor);
        mCountTextPaint.setTextSize(context.getResources().getDimension(R.dimen.hoop_count_text_size));

        typedArray.recycle();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        mWidth = getDefaultSize(widthSize, widthMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override protected void onDraw(Canvas canvas) {
        float cx = 0, cy = 0;
        switch (mState) {
            case STATE_NORMAL:
                //先画背景大圆
                Log.d(TAG, "getRight:" + getRight() + ", getBottom:" + getBottom());
                if (mDirection == 1) {
                    cx = mWidth - mBigRadius;
                    cy = mHeight - mBigRadius;
                    float left = cx - mBigRadius;
                    float right = cx + mBigRadius;
                    float top = cy - mBigRadius;
                    float bottom = cy + mBigRadius;
                    Log.d(TAG, "cx:" + cx + ", cy:" + cy);
                    Log.d(TAG, "left:" + left + ", right:" + right + ", top:" + top + ", bottom:" + bottom);
                    canvas.drawRoundRect(left, top, right, bottom, mBigRadius, mBigRadius, mPopPaint);
                    canvas.drawCircle(cx, cy, mBigRadius, mBgPaint);
                } else if (mDirection == 2) {
                    cx = mBigRadius;
                    cy = mHeight - mBigRadius;
                    canvas.drawCircle(cx, cy, mBigRadius, mBgPaint);
                }
                break;
            case STATE_SHRINK:
                if (mDirection == 1) {
                    cx = mWidth - mBigRadius;
                    cy = mHeight - mBigRadius;
                    float left = cx - mBigRadius - mChange;
                    float right = cx + mBigRadius;
                    float top = cy - mBigRadius;
                    float bottom = cy + mBigRadius;
                    canvas.drawRoundRect(left, top, right, bottom, mBigRadius, mBigRadius, mPopPaint);
                    canvas.drawCircle(cx, cy, mBigRadius, mBgPaint);

                    if (mChange > (4 * mSmallRadius + 3 * 10) && mChange <= (6 * mSmallRadius + 4 * 10)) {
                        canvas.drawCircle(cx - 10 - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawCircle(cx - mBigRadius - mSmallRadius - 10, cy, mSmallRadius, mBgPaint);
                        canvas.drawCircle(cx - mBigRadius - 3 * mSmallRadius - 2 * 10, cy, mSmallRadius, mBgPaint);
                    } else if (mChange > 2 * (mSmallRadius + 10) && mChange <= (4 * mSmallRadius + 3 * 10)) {
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawCircle(cx - mBigRadius - mSmallRadius - 10, cy, mSmallRadius, mBgPaint);
                    } else if (mChange > 1 && mChange <= 2 * (mSmallRadius + 10)) {
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                    }

                } else if (mDirection == 2) {
                    cx = mBigRadius;
                    cy = getBottom() - mBigRadius;
                    canvas.drawCircle(cx, cy, mBigRadius, mBgPaint);
                }
                break;
            case STATE_EXPAND:
                if (mDirection == 1) {
                    cx = mWidth - mBigRadius;
                    cy = mHeight - mBigRadius;
                    float left = cx - mBigRadius - mChange;
                    float right = cx + mBigRadius;
                    float top = cy - mBigRadius;
                    float bottom = cy + mBigRadius;
                    canvas.drawRoundRect(left, top, right, bottom, mBigRadius, mBigRadius, mPopPaint);
                    canvas.drawCircle(cx, cy, mBigRadius, mBgPaint);

                    if (mChange > 1 && mChange <= 2 * (mSmallRadius + 10)) {
                        //除了画小圆，还要在小圆上画文字
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("100", cx-mChange-20, cy+15, mTextPaint);
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("10", cx-mChange-20, cy+15, mTextPaint);
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("1", cx-mChange, cy+15, mTextPaint);
                    } else if (mChange > 2 * (mSmallRadius + 10) && mChange <= (4 * mSmallRadius + 3 * 10)) {
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("100", cx-mChange-20, cy+15, mTextPaint);
                        canvas.drawCircle(cx - mBigRadius - mSmallRadius - 10, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("10", cx - mBigRadius - mSmallRadius - 20, cy+15, mTextPaint);
                        canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("1", cx-mChange-10, cy+15, mTextPaint);
                    } else if (mChange > (4 * mSmallRadius + 3 * 10) && mChange <= (6 * mSmallRadius + 4 * 10)) {
                        canvas.drawCircle(cx - 10 - mChange, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("100", cx-mChange-50, cy + 15, mTextPaint);
                        canvas.drawCircle(cx - mBigRadius - 3 * mSmallRadius - 2 * 10, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("10", cx - mBigRadius - 3 * mSmallRadius - 50, cy+15, mTextPaint);
                        canvas.drawCircle(cx - mBigRadius - mSmallRadius - 10, cy, mSmallRadius, mBgPaint);
                        canvas.drawText("1", cx - mBigRadius - mSmallRadius - 25, cy+15, mTextPaint);
                    }

                } else if (mDirection == 2) {
                    cx = mBigRadius;
                    cy = mHeight - mBigRadius;
                    float left = cx;
                    float right = cx + mChange;
                    float top = cy - mBigRadius;
                    float bottom = cy + mBigRadius;
                    canvas.drawRect(left, top, right, bottom, mPopPaint);
                }
                break;
        }
        circle = new PointF(cx, cy);
        circleOne = new PointF(cx - mBigRadius - mSmallRadius - 10, cy);
        circleTwo = new PointF(cx - mBigRadius - 3 * mSmallRadius - 2 * 10, cy);
        circleThree = new PointF(cx - (5 * mSmallRadius + mButtonCount * 10), cy);
        //再画大圆里面的文字
        StaticLayout layout = new StaticLayout(mText, mTextPaint, (int)(mBigRadius * Math.sqrt(2)), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
        if (mDirection == 1) {
            canvas.translate(getRight() - mBigRadius * 1.7f, mHeight - mBigRadius * 1.8f);
        } else if (mDirection == 2) {
            canvas.translate(mBigRadius * (float) (1 - Math.sqrt(2) / 2), mHeight - mBigRadius * 1.8f);
        }
        layout.draw(canvas);
        canvas.save();

        // 再画圆上面表示金币数的文字
        canvas.translate(0, -25);
        //计算文字的宽度
        float textWidth = mCountTextPaint.measureText(mCount, 0, mCount.length());
        canvas.drawText(mCount, 0, mCount.length(), (2 * mBigRadius - textWidth - 35) / 2, 0.2f, mCountTextPaint);

    }


    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //如果点击的时候动画在进行，不处理
                if (mIsRun) return true;
                PointF pointF = new PointF(event.getX(), event.getY());
                if (isPointInCircle(pointF, circle, mBigRadius)) {
                    //如果触摸点在大圆内，根据弹出方向弹出或者收缩按钮
                    if ((mState == STATE_SHRINK || mState == STATE_NORMAL) && !mIsRun) {
                        //展开
                        mIsRun = true;
                        mState = STATE_EXPAND;
                        //画弹出框背景
                        if (mDirection == 1) {
                            showPopMenu(1);
                        } else if (mDirection == 2) {
                            showPopMenu(2);
                        }
                    } else {
                        //收缩
                        mIsRun = true;
                        mState = STATE_SHRINK;
                        if (mDirection == 1) {
                            hidePopMenun(1);
                        } else if (mDirection == 2) {
                            hidePopMenun(2);
                        }
                    }
                } else if (isPointInCircle(pointF, circleOne, mSmallRadius)) {
                    listener.clickButton(this, 1);
                } else if (isPointInCircle(pointF, circleTwo, mSmallRadius)) {
                    listener.clickButton(this, 10);
                } else if (isPointInCircle(pointF, circleThree, mSmallRadius)) {
                    listener.clickButton(this, 100);
                } else {
                    hidePopMenun(mDirection);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 根据弹出方向弹出背景框
     * @param direction
     */
    private void showPopMenu(int direction) {
        if (direction == 1) { //向左弹出
            if (mState == STATE_EXPAND) {
                ValueAnimator animator = ValueAnimator.ofInt(0, mChangeWidth);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (mIsRun) {
                            mChange = (int) animation.getAnimatedValue();
                            invalidate();
                        } else {
                            animation.cancel();
                        }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mIsRun = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mIsRun = false;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mIsRun = false;
                    }
                });
                animator.setDuration(500);
                animator.start();
            }
        }
    }

    private void hidePopMenun(int direction) {
        if (direction == 1) {
            if (mState == STATE_SHRINK) {
                ValueAnimator animator = ValueAnimator.ofInt(mChangeWidth, 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (mIsRun) {
                            mChange = (int) animation.getAnimatedValue();
                            invalidate();
                        } else {
                            animation.cancel();
                        }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mIsRun = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mIsRun = false;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mIsRun = false;
                    }
                });
                animator.setDuration(500);
                animator.start();
            }
        }
    }

    /**
     * 判断点是否在圆内
     *
     * @param pointF 待确定点
     * @param circle 圆心
     * @param radius 半径
     * @return true在圆内
     */
    private boolean isPointInCircle(PointF pointF, PointF circle, float radius) {
        return Math.pow((pointF.x - circle.x), 2) + Math.pow((pointF.y - circle.y), 2) <= Math.pow(radius, 2);
    }

    interface OnClickButtonListener {
        void clickButton(View view, int num);
    }

    public void setOnClickButtonListener(OnClickButtonListener listener) {
        this.listener = listener;
    }

    public void setCount(int count) {
        mCount = String.valueOf(Integer.parseInt(mCount) + count);
        mState = STATE_NORMAL;
        invalidate();
    }

}
