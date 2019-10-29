package com.nmd.easy.plate;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Random;


/**
 * Created by biaozhang on 2019/10/22 10:54
 * 幸运转盘
 */
public class LuckyPlate extends View {
    private static final String TAG = "LuckyPlateLog";
    private OnAnimatorListener mOnAnimatorListener;
    private int division = 2;
    private String[] mContents;
    public int DEFAULT_VIEW_SIZE;
    private int mScreenWidth;
    private int mScreenHeight;
    private int viewWidth;
    private int viewHeight;
    private Paint mCirclePaint;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private int randomPos;


    public LuckyPlate(Context context) {
        this(context, null);
    }

    public LuckyPlate(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckyPlate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dealWithPadding();
        int pointX = viewWidth / 2;
        int pointY = viewHeight / 2;
        canvas.translate(pointX, pointX);
        canvas.drawCircle(0, 0, pointX, mCirclePaint);
        canvas.drawLine(0, 0, 0, -pointY, mLinePaint);
        divideCircle(canvas, pointX);
    }

    /**
     * wrap_content处理
     * 默认处理为手机屏幕宽高中的最小值
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DEFAULT_VIEW_SIZE = Math.min(mScreenWidth, mScreenHeight) * 4 / 5;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heighthMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heighthMode == MeasureSpec.AT_MOST) {// 用户宽高都设置为 wrap_content时
            setMeasuredDimension(DEFAULT_VIEW_SIZE, DEFAULT_VIEW_SIZE);
        } else if (widthMode == MeasureSpec.AT_MOST) {// 当宽设置了wrap_content时
            setMeasuredDimension(DEFAULT_VIEW_SIZE, heightSize);
        } else if (heighthMode == MeasureSpec.AT_MOST) {// 当高设置了wrap_content时
            setMeasuredDimension(widthSize, DEFAULT_VIEW_SIZE);
        } else {
            setMeasuredDimension(DEFAULT_VIEW_SIZE, DEFAULT_VIEW_SIZE); //默认大小，new对象的方式时
        }
    }

    private void init() {
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.BLUE);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(3);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.GRAY);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(40);
        mCirclePaint.setTextSkewX(.5f);

    }

    /**
     * 吧圆划分成整块
     * ps:
     * 1、360除不尽的奇数最后一块会多几度
     * 2、绘制文字居中。循环旋转画布指定角度n，旋转n/2时绘制文字，旋转n度时绘制线。
     */
    private void divideCircle(Canvas canvas, int radius) {
        if (mContents == null) {
            dealWithEmptyStringArr();
        }

        division = mContents.length;
        int flag = 0;
        float textAngle = 360 / division / 2;
        for (int i = 0; i < division * 2; i++) {
            if (i == division * 2 - 1) {
                canvas.rotate(360 - textAngle * (division * 2 - 1));
            } else {
                canvas.rotate(textAngle);
                flag++;
                if (flag % 2 != 0) {
                    Rect rect = new Rect();
                    mTextPaint.getTextBounds(mContents[i / 2], 0, mContents[i / 2].length(), rect);
                    canvas.drawText(mContents[i / 2], -rect.width() / 2, -(radius - rect.height() - 100), mTextPaint);
                } else {
                    canvas.drawLine(0, 0, 0, -radius, mLinePaint);
                }
            }
        }
    }

    private void dealWithEmptyStringArr() {
        mContents = new String[]{"火锅", "火锅", "火锅", "火锅"};
    }

    private void dealWithPadding() {
        final int paddingLeft = getPaddingStart();
        final int paddingRight = getPaddingEnd();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        viewWidth = getWidth() - paddingLeft - paddingRight;
        viewHeight = getHeight() - paddingTop - paddingBottom;
    }


    public void setContents(String[] contents) {
        mContents = contents;
        invalidate();
    }

    /**
     * @param pos 指定位置
     * @functuion 转动到指定位置（索引从0开始）
     * ps：如果 pos = -1 则随机转动，如果指定某个值，则转到某个指定区域。
     */
    public void startRotate(int pos) {
        if (pos < division) {
            int targetItem;
            ValueAnimator animator;
            if (pos == -1) {//随机
                randomPos = randomPos();
                targetItem = division - 1 - randomPos;

            } else {//用户指定位置
                targetItem = division - 1 - pos;
            }

            animator = ValueAnimator.ofFloat(0, 360 * 5 + 360 / division / 2 + targetItem * (360 / division));

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float degree = (float) animation.getAnimatedValue();
                    setRotation(degree);
                }

            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOnAnimatorListener.finish(randomPos);
                }
            });
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(5000);
            animator.start();

        } else {
            throw new IllegalArgumentException("param out of bound");
        }
    }

    public int randomPos() {
        return new Random().nextInt(division);
    }

    public void addOnAnimatorListener(OnAnimatorListener onAnimatorListener) {
        this.mOnAnimatorListener = onAnimatorListener;
    }


    public interface OnAnimatorListener {
        void finish(int randomPos);
    }
}