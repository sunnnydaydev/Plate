>最近在工作中碰到了抽奖转盘的类似需求，本来打算github找找，结果发现好多都是转盘内容固定，不支持动态改变的，于是就亲自尝试一番，也就当练习了下自定义view。作为应届，知识还是不够丰富的，所以就简单的实现了下。

###### 1、效果图
>由于gif图内存有限制，请观看视屏。
>[传送门](https://github.com/sunnnydaydev/Plate/blob/master/demo/video.mp4)

###### 2、实现思路
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191029180217479.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM4MzUwNjM1,size_16,color_FFFFFF,t_70)

>其实自定义view的难点一部分在于==大量数学计算==、一部分在于==view的分解==、

（1）view的分解
>1、画圆
>2、画线（这里有个小技巧：循环旋转画布为相应的角度即可）
>3、绘制文字（小技巧：文字居中的处理，获得文字的大小，设置坐标）

（2）数学计算
>本自定义view还算比较简单
>1、view的默认大小：手机屏幕宽高中最小值的五分之四
>2、角度计算360/份数（数组内容个数）
>3、圆整分
>>其实一些奇数是不能整分的例如7,360是整除不尽的这里就采取循环设置前六分为 360/7取整的，最后一份为剩余的。其实设置份数为n 每一份都是采取这样的策略，前面n-1分均分。最后一份为360减去剩余的。
>
>4、文字的居中绘制
>>这里牵涉到一些小技巧，画布旋转n度时画线，则旋转n/2绘制文字。而且我们还可以会的绘制文字的一个Rect，这时可计算出文字坐标。在居中显示即可。
>
>5、播放动画随机旋转位置或者指定旋转位置（如下图）
>>假如位置0、1，我们随机数为1，则旋转的圈数为：圈数*360+a（a需要我们手动计算下，圈数随便指定即可，只是相当于多转了n圈）

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191029184507163.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM4MzUwNjM1,size_16,color_FFFFFF,t_70)


###### 3、其他知识点
>1、属性动画ValueAnimator的灵活运用
>2、自定义view的默认大小处理（wrap-content）
>ps：这里考验掌握的知识

###### 4、源码及其使用
```java
/**
 * Created by sunnyDay on 2019/10/22 10:54
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
```
>使用参考：
>[简单使用](https://github.com/sunnnydaydev/Plate/blob/master/app/src/main/java/com/nmd/easy/plate/MainActivity.java)


###### 5、小结
>简单的实现了下，为了方便没有自定义xml属性啥的。发现对知识的了解又加深了一步，快乐啦！溜溜球。。。

###### 6、使用
>1、如果想直接拿来用，需求和这里一致，那直接把类复制走即可。参考使用案例就o了
>2、也欢迎大家交流学习，改写添加图片等新的功能。
>[项目源码](https://github.com/sunnnydaydev/Plate)

