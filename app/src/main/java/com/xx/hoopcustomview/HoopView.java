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
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by shenhuniurou
 * on 2016/11/11.
 */

public class HoopView extends View {

    private int mThemeColor = Color.YELLOW;//按钮颜色和菜单按钮的颜色
    private int mTextColor = Color.WHITE;//按钮内文字的颜色
    private String mText;//按钮内文字
    private String mCount;//目前送的金币数量
    private Paint mPopPaint;//弹出框背景画笔
    private Paint mBgPaint;//大圆画笔
    private TextPaint mTextPaint;
    private TextPaint mCountTextPaint;
    private PointF circle;//大圆的圆心
    private float mBigRadius;//大圆的半径
    private float mSmallRadius;//小圆的半径
    private int margin;//按钮之间的间距
    private int countMargin;//金币数与大圆的间距

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
    //正在展开
    public static final int STATE_EXPANDING = 3;
    //正在收缩
    public static final int STATE_SHRINKING = 4;

    /*----------view的宽高-----------*/
    private int mWidth;
    private int mHeight;

    private int mChangeWidth;//背景框改变的长度
    private int mChange;//背景框当前改变的值

    // 大圆圆心
    float cx = 0, cy = 0;
    float left, right, top, bottom;
    String[] mDatas;

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
        mText = typedArray.getString(R.styleable.HoopView_text);
        mCount = typedArray.getString(R.styleable.HoopView_count);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(mThemeColor);
        mBgPaint.setAlpha(190);
        mBgPaint.setStyle(Paint.Style.FILL);

        mPopPaint = new Paint();
        mPopPaint.setAntiAlias(true);
        mPopPaint.setColor(Color.LTGRAY);
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

        mBigRadius = context.getResources().getDimension(R.dimen.hoop_big_circle_radius);
        mSmallRadius = context.getResources().getDimension(R.dimen.hoop_small_circle_radius);
        margin = (int) context.getResources().getDimension(R.dimen.hoop_margin);
        mHeight = (int) context.getResources().getDimension(R.dimen.hoop_view_height);
        countMargin = (int) context.getResources().getDimension(R.dimen.hoop_count_margin);

        mDatas = new String[] {"1", "10", "100"};
        // 计算背景框改变的长度，默认是三个按钮
        mChangeWidth = (int) (2 * mSmallRadius * 3 + 4 * margin);

    }


    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        mWidth = getDefaultSize(widthSize, widthMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);

        // 此时才测出了mWidth值，再计算圆心坐标及相关值
        cx = mWidth - mBigRadius;
        cy = mHeight - mBigRadius;
        // 大圆圆心
        circle = new PointF(cx, cy);
        // 三个按钮的圆心
        circleOne = new PointF(cx - mBigRadius - mSmallRadius - margin, cy);
        circleTwo = new PointF(cx - mBigRadius - 3 * mSmallRadius - 2 * margin, cy);
        circleThree = new PointF(cx - mBigRadius - 5 * mSmallRadius - 3 * margin, cy);
        // 初始的背景框的边界即为大圆的四个边界点
        top = cy - mBigRadius;
        bottom = cy + mBigRadius;
    }


    /**
     * 画背景大圆
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {
        left = cx - mBigRadius;
        right = cx + mBigRadius;
        canvas.drawCircle(cx, cy, mBigRadius, mBgPaint);
    }


    /**
     * 画大圆上面表示金币数的文字
     * @param canvas
     */
    private void drawCountText(Canvas canvas) {
        canvas.translate(0, -countMargin);
        //计算文字的宽度
        float textWidth = mCountTextPaint.measureText(mCount, 0, mCount.length());
        canvas.drawText(mCount, 0, mCount.length(), (2 * mBigRadius - textWidth - 35) / 2, 0.2f, mCountTextPaint);
    }


    /**
     * 画大圆内的文字
     * @param canvas
     */
    private void drawCircleText(Canvas canvas) {
        StaticLayout layout = new StaticLayout(mText, mTextPaint, (int) (mBigRadius * Math.sqrt(2)), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
        canvas.translate(mWidth - mBigRadius * 1.707f, mHeight - mBigRadius * 1.707f);
        layout.draw(canvas);
        canvas.save();
    }


    /**
     * 画弹出框展开的过程
     * @param canvas
     */
    private void drawExpanding(Canvas canvas) {
        left = cx - mBigRadius - mChange;
        right = cx + mBigRadius;
        canvas.drawRoundRect(left, top, right, bottom, mBigRadius, mBigRadius, mPopPaint);
        if ((mChange > 0) && (mChange <= 2 * mSmallRadius + margin)) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - (mBigRadius - mSmallRadius) - mChange, cy + 15, mTextPaint);
        } else if ((mChange > 2 * mSmallRadius + margin) && (mChange <= 4 * mSmallRadius + 2 * margin)) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mBigRadius - mSmallRadius - margin, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - mBigRadius - mSmallRadius - margin - 20, cy + 15, mTextPaint);

            // 绘制第二个按钮
            canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
            // 绘制第二个按钮内的文字
            canvas.drawText(mDatas[1], cx - mChange - 20, cy + 15, mTextPaint);
        } else if ((mChange > 4 * mSmallRadius + 2 * margin) && (mChange <= 6 * mSmallRadius + 3 * margin)) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mBigRadius - mSmallRadius - margin, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - mBigRadius - mSmallRadius - margin - 16, cy + 15, mTextPaint);

            // 绘制第二个按钮
            canvas.drawCircle(cx - mBigRadius - 3 * mSmallRadius - 2 * margin, cy, mSmallRadius, mBgPaint);
            // 绘制第二个按钮内的文字
            canvas.drawText(mDatas[1], cx - mBigRadius - 3 * mSmallRadius - 2 * margin - 25, cy + 15, mTextPaint);

            // 绘制第三个按钮
            canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
            // 绘制第三个按钮内的文字
            canvas.drawText(mDatas[2], cx - mChange - 34, cy + 15, mTextPaint);
        } else  if (mChange > 6 * mSmallRadius + 3 * margin) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mBigRadius - mSmallRadius - margin, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - mBigRadius - mSmallRadius - margin - 16, cy + 15, mTextPaint);

            // 绘制第二个按钮
            canvas.drawCircle(cx - mBigRadius - 3 * mSmallRadius - 2 * margin, cy, mSmallRadius, mBgPaint);
            // 绘制第二个按钮内的文字
            canvas.drawText(mDatas[1], cx - mBigRadius - 3 * mSmallRadius - 2 * margin - 25, cy + 15, mTextPaint);

            // 绘制第三个按钮
            canvas.drawCircle(cx - mBigRadius - 5 * mSmallRadius - 3 * margin, cy, mSmallRadius, mBgPaint);
            // 绘制第三个按钮内的文字
            canvas.drawText(mDatas[2], cx - mBigRadius - 5 * mSmallRadius - 3 * margin - 34, cy + 15, mTextPaint);
        }
        drawCircle(canvas);

    }


    /**
     * 画弹出框收缩的过程
     * @param canvas
     */
    private void drawShrinking(Canvas canvas) {

        left = cx - mBigRadius - mChange;
        right = cx + mBigRadius;
        canvas.drawRoundRect(left, top, right, bottom, mBigRadius, mBigRadius, mPopPaint);
        if ((mChange > 0) && (mChange <= 2 * mSmallRadius + margin)) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - (mBigRadius - mSmallRadius) - mChange, cy + 15, mTextPaint);
        } else if ((mChange > 2 * mSmallRadius + margin) && (mChange <= 4 * mSmallRadius + 2 * margin)) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mBigRadius - mSmallRadius - margin, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - mBigRadius - mSmallRadius - margin - 16, cy + 15, mTextPaint);

            // 绘制第二个按钮
            canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
            // 绘制第二个按钮内的文字
            canvas.drawText(mDatas[1], cx - mChange - 20, cy + 15, mTextPaint);
        } else if ((mChange > 4 * mSmallRadius + 2 * margin) && (mChange <= 6 * mSmallRadius + 3 * margin)) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mBigRadius - mSmallRadius - margin, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - mBigRadius - mSmallRadius - margin - 16, cy + 15, mTextPaint);

            // 绘制第二个按钮
            canvas.drawCircle(cx - mBigRadius - 3 * mSmallRadius - 2 * margin, cy, mSmallRadius, mBgPaint);
            // 绘制第二个按钮内的文字
            canvas.drawText(mDatas[1], cx - mBigRadius - 3 * mSmallRadius - 2 * margin - 25, cy + 15, mTextPaint);

            // 绘制第三个按钮
            canvas.drawCircle(cx - mChange, cy, mSmallRadius, mBgPaint);
            // 绘制第三个按钮内的文字
            canvas.drawText(mDatas[2], cx - mChange - 35, cy + 15, mTextPaint);
        } else if (mChange > 6 * mSmallRadius + 3 * margin) {
            // 绘制第一个按钮
            canvas.drawCircle(cx - mBigRadius - mSmallRadius - margin, cy, mSmallRadius, mBgPaint);
            // 绘制第一个按钮内的文字
            canvas.drawText(mDatas[0], cx - mBigRadius - mSmallRadius - margin - 16, cy + 15, mTextPaint);

            // 绘制第二个按钮
            canvas.drawCircle(cx - mBigRadius - 3 * mSmallRadius - 2 * margin, cy, mSmallRadius, mBgPaint);
            // 绘制第二个按钮内的文字
            canvas.drawText(mDatas[1], cx - mBigRadius - 3 * mSmallRadius - 2 * margin - 25, cy + 15, mTextPaint);

            // 绘制第三个按钮
            canvas.drawCircle(cx - mBigRadius - 5 * mSmallRadius - 3 * margin, cy, mSmallRadius, mBgPaint);
            // 绘制第三个按钮内的文字
            canvas.drawText(mDatas[2], cx - mBigRadius - 5 * mSmallRadius - 3 * margin - 34, cy + 15, mTextPaint);
        }
        drawCircle(canvas);
    }


    @Override protected void onDraw(Canvas canvas) {
        switch (mState) {
            case STATE_NORMAL:
                drawCircle(canvas);
                break;
            case STATE_SHRINK:
            case STATE_SHRINKING:
                drawShrinking(canvas);
                break;
            case STATE_EXPAND:
            case STATE_EXPANDING:
                drawExpanding(canvas);
                break;
        }
        drawCircleText(canvas);
        drawCountText(canvas);
    }


    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //如果点击的时候动画在进行，不处理
                if (mIsRun) return true;
                PointF pointF = new PointF(event.getX(), event.getY());
                if (isPointInCircle(pointF, circle, mBigRadius)) { //如果触摸点在大圆内，根据弹出方向弹出或者收缩按钮
                    if ((mState == STATE_SHRINK || mState == STATE_NORMAL) && !mIsRun) {
                        //展开
                        mIsRun = true;//这是必须先设置true，因为onAnimationStart在onAnimationUpdate之后才调用
                        showPopMenu();
                    } else {
                        //收缩
                        mIsRun = true;
                        hidePopMenu();
                    }
                } else { //触摸点不在大圆内
                    if (mState == STATE_EXPAND) { //如果是展开状态
                        if (isPointInCircle(pointF, circleOne, mSmallRadius)) {
                            listener.clickButton(this, Integer.parseInt(mDatas[0]));
                        } else if (isPointInCircle(pointF, circleTwo, mSmallRadius)) {
                            listener.clickButton(this, Integer.parseInt(mDatas[1]));
                        } else if (isPointInCircle(pointF, circleThree, mSmallRadius)) {
                            listener.clickButton(this, Integer.parseInt(mDatas[2]));
                        }
                        mIsRun = true;
                        hidePopMenu();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 弹出背景框
     */
    private void showPopMenu() {
        if (mState == STATE_SHRINK || mState == STATE_NORMAL) {
            ValueAnimator animator = ValueAnimator.ofInt(0, mChangeWidth);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    if (mIsRun) {
                        mChange = (int) animation.getAnimatedValue();
                        invalidate();
                    } else {
                        animation.cancel();
                        mState = STATE_NORMAL;
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mIsRun = true;
                    mState = STATE_EXPANDING;
                }


                @Override public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mIsRun = false;
                    mState = STATE_NORMAL;
                }


                @Override public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIsRun = false;
                    //动画结束后设置状态为展开
                    mState = STATE_EXPAND;
                }
            });
            animator.setDuration(500);
            animator.start();
        }
    }


    /**
     * 隐藏弹出框
     */
    private void hidePopMenu() {
        if (mState == STATE_EXPAND) {
            ValueAnimator animator = ValueAnimator.ofInt(mChangeWidth, 0);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    if (mIsRun) {
                        mChange = (int) animation.getAnimatedValue();
                        invalidate();
                    } else {
                        animation.cancel();
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mIsRun = true;
                    mState = STATE_SHRINKING;
                }


                @Override public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mIsRun = false;
                    mState = STATE_EXPAND;
                }


                @Override public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIsRun = false;
                    //动画结束后设置状态为收缩
                    mState = STATE_SHRINK;
                }
            });
            animator.setDuration(500);
            animator.start();
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

    /**
     * 设置金币数
     */
    public void setCount(int count) {
        mCount = String.valueOf(Integer.parseInt(mCount) + count);
    }


    interface OnClickButtonListener {
        void clickButton(View view, int num);
    }


    public void setOnClickButtonListener(OnClickButtonListener listener) {
        this.listener = listener;
    }

}
