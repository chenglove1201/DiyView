package com.icandemy.diyview.loopbanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import com.icandemy.diyview.R;

import java.util.LinkedList;

/**
 * Created by ICAN on 2017/6/12.
 */

public class LoopBanner extends ViewGroup {
    private ImageLoader imageLoader;
    private BannerImage bannerImage;
    private int layoutWidth, layoutHeight;
    private Context context;
    private Paint paintNormal, paintSelected;
    private Object[] images;
    private int imageCount;
    /**
     * 指示器间隔
     */
    private int dotMargin = 5;
    /**
     * 指示器直径
     */
    private int dotDiameter = 10;
    /**
     * 指示器未选择时颜色
     */
    private int indicationNormal = Color.GRAY;
    /**
     * 指示器被选择时颜色
     */
    private int indicationSelected = Color.RED;
    /**
     * 自动滚动持续时间
     */
    private int scrollDuration = 250;
    /**
     * 抬起时允许视图切换的最小滑动速度
     */
    private int minVelocity = 2500;
    /**
     * 指示器位置
     */
    private int position;

    public LoopBanner(Context context) {
        this(context, null);
    }

    public LoopBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoopBanner);
        dotDiameter = typedArray.getDimensionPixelSize(R.styleable.LoopBanner_indicationDiameter, dotDiameter);
        dotMargin = typedArray.getDimensionPixelSize(R.styleable.LoopBanner_indicationMargin, dotMargin);
        scrollDuration = typedArray.getInteger(R.styleable.LoopBanner_scrollDuration, scrollDuration);
        indicationNormal = typedArray.getInteger(R.styleable.LoopBanner_indicationNormal, indicationNormal);
        indicationSelected = typedArray.getInteger(R.styleable.LoopBanner_indicationSelected, indicationSelected);
        typedArray.recycle();

        init();
    }

    private void init() {
        setWillNotDraw(false);

        bannerImage = new BannerImage(context);
        addView(bannerImage);

        paintNormal = new Paint();
        paintNormal.setAntiAlias(true);
        paintNormal.setColor(indicationNormal);

        paintSelected = new Paint();
        paintSelected.setAntiAlias(true);
        paintSelected.setColor(indicationSelected);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST)
            layoutWidth = 600;
        else
            layoutWidth = MeasureSpec.getSize(widthMeasureSpec);

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST)
            layoutHeight = layoutWidth / 2;
        else
            layoutHeight = MeasureSpec.getSize(heightMeasureSpec);

        getChildAt(0).measure(MeasureSpec.makeMeasureSpec(layoutWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.EXACTLY));

        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //设置轮播图位置
        getChildAt(0).layout(0, 0, layoutWidth, layoutHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int allDotWidth = imageCount * dotDiameter + (imageCount - 1) * dotMargin;
        int cy = layoutHeight - 10 - dotDiameter / 2;

        for (int i = 0; i < imageCount; i++) {
            if (i == position)
                canvas.drawCircle((layoutWidth - allDotWidth) / 2 + (dotDiameter + dotMargin) * i + dotDiameter / 2,
                        cy,
                        dotDiameter / 2, paintSelected);
            else
                canvas.drawCircle((layoutWidth - allDotWidth) / 2 + (dotDiameter + dotMargin) * i + dotDiameter / 2,
                        cy,
                        dotDiameter / 2, paintNormal);
        }
    }

    public void setImageLoader(Object[] images, ImageLoader imageLoader) {
        this.images = images;
        this.imageCount = images.length;
        this.imageLoader = imageLoader;

        bannerImage.setImage();
    }

    public interface ImageLoader {
        void loaderImage(Context context, ImageView imageView, Object source);
    }

    /**
     * 设置指示器位置
     */
    public void setIndicationPosition() {
        invalidate();
    }

    /**
     * 轮播图
     */
    private class BannerImage extends ViewGroup {
        private VelocityTracker velocityTracker;
        private Scroller scroller;
        private int showPosition = 0;
        private final int LOOP_MESSAGE = 1;
        private final int DELAY_TIME = 3000;
        /**
         * 手指放下时的x坐标
         */
        private int startX;
        /**
         * 手指放下时已滚动的距离
         */
        private int startScrollX;
        /**
         * 子view的排列顺序
         */
        private LinkedList<Integer> childViewOrder = new LinkedList<>();
        /**
         * 手指是否抬起
         */
        private boolean isUp = true;
        /**
         * 是否重绘布局
         */
        private boolean reLayout = true;
        /**
         * 向左滑动时，每交换一次顺序，加一；向右滑动时，每交换一次顺序，减一
         */
        private int exchangeCount;
        /**
         * 手指按下时当前exchangeCount的值
         */
        private int downExchangeCount;

        public BannerImage(Context context) {
            this(context, null);
        }

        public BannerImage(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public BannerImage(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            setWillNotDraw(false);
            scroller = new Scroller(context);
            velocityTracker = VelocityTracker.obtain();
        }

        public void setImage() {
            if (imageCount >= 3) {
                ImageView imageView0 = new ImageView(context);
                ImageView imageView1 = new ImageView(context);
                ImageView imageView2 = new ImageView(context);

                imageView0.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView2.setScaleType(ImageView.ScaleType.FIT_XY);

                addView(imageView0);
                addView(imageView1);
                addView(imageView2);

                childViewOrder.add(0);
                childViewOrder.add(1);
                childViewOrder.add(2);

                imageLoader.loaderImage(context, imageView0, images[imageCount - 1]);
                imageLoader.loaderImage(context, imageView1, images[showPosition]);
                imageLoader.loaderImage(context, imageView2, images[showPosition + 1]);

                //开启自动播放
                handler.sendEmptyMessageDelayed(LOOP_MESSAGE, DELAY_TIME);
            } else
                Log.e("LoopBanner", "图片数量最小为3");
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (reLayout) {
                if (getChildCount() > 0) {
                    getChildAt(0).layout(-layoutWidth,
                            0,
                            0,
                            layoutHeight);
                    getChildAt(1).layout(0,
                            0,
                            layoutWidth,
                            layoutHeight);
                    getChildAt(2).layout(layoutWidth,
                            0,
                            layoutWidth * 2,
                            layoutHeight);
                    reLayout = false;
                }
            }
        }

        /**
         * 滚动过程中监听子view的排序及布局，根据滚动距离进行调整，让其中间的子view始终可见
         */
        @Override
        public void computeScroll() {
            super.computeScroll();
            int scrollX = getScrollX();
            if (scrollX <= (layoutWidth / 2 + layoutWidth * (exchangeCount - 1))) {
                Log.d("LoopBanner", "向右滑动，最后一个子view放置最前");
                getChildAt(childViewOrder.get(2)).layout(getChildAt(childViewOrder.get(0)).getLeft() - layoutWidth,
                        0,
                        getChildAt(childViewOrder.get(0)).getLeft(),
                        layoutHeight);
                int swap = childViewOrder.removeLast();
                childViewOrder.addFirst(swap);
                exchangeCount--;

                if (showPosition == 0)
                    showPosition = imageCount - 1;
                else
                    showPosition--;

                imageLoader.loaderImage(context, ((ImageView) getChildAt(childViewOrder.get(0))),
                        images[showPosition == 0 ? imageCount - 1 : showPosition - 1]);
                if (isUp)
                    bannerChangeEvent(exchangeCount);
            }
            if (scrollX > (layoutWidth / 2 + layoutWidth * exchangeCount)) {
                Log.d("LoopBanner", "向左滑动，第一个子view放置最后");
                getChildAt(childViewOrder.get(0)).layout(getChildAt(childViewOrder.get(2)).getRight(),
                        0,
                        getChildAt(childViewOrder.get(2)).getRight() + layoutWidth,
                        layoutHeight);
                int swap = childViewOrder.removeFirst();
                childViewOrder.addLast(swap);
                exchangeCount++;

                showPosition++;
                if (showPosition == imageCount)
                    showPosition = 0;

                imageLoader.loaderImage(context, ((ImageView) getChildAt(childViewOrder.get(2))),
                        images[showPosition == imageCount - 1 ? 0 : showPosition + 1]);
                if (isUp)
                    bannerChangeEvent(exchangeCount);
            }
            if (scroller.computeScrollOffset()) {
                scrollTo(scroller.getCurrX(), scroller.getCurrY());
                invalidate();
            }
        }

        private void bannerChangeEvent(int exchangeCount) {
            if (exchangeCount > 0)
                position = exchangeCount % imageCount;
            else {
                int flag = imageCount - (-exchangeCount % imageCount);
                position = flag == imageCount ? 0 : flag;
            }
            setIndicationPosition();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            velocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int scrollX = getScrollX();
                    startX = (int) event.getX();
                    startScrollX = scrollX;
                    if (scroller.computeScrollOffset())
                        scroller.abortAnimation();
                    //按下时暂停自动播放
                    stopLoop();
                    isUp = false;
                    downExchangeCount = exchangeCount;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int currentX = (int) event.getX();
                    scrollTo(-(currentX - startX) + startScrollX, 0);
                    break;
                case MotionEvent.ACTION_UP:
                    isUp = true;
                    velocityTracker.computeCurrentVelocity(1000, minVelocity);
                    float velocityX = velocityTracker.getXVelocity();
                    //抬起时启动自动播放
                    startLoop();
                    int mScrollX = getScrollX();
                    int instance;
                    int direction = mScrollX - startScrollX;
                    if (mScrollX > 0) {//视图滑到右半部
                        if (direction > 0) { //向左滑
                            instance = mScrollX % layoutWidth;
                            leftDirectionScroll(velocityX, mScrollX, instance);
                        } else {//向右滑
                            instance = layoutWidth - mScrollX % layoutWidth;
                            rightDirectionScroll(velocityX, mScrollX, instance);
                        }
                    } else {//视图滑到左半部
                        if (direction > 0) {//向左滑
                            instance = layoutWidth + mScrollX % layoutWidth;
                            leftDirectionScroll(velocityX, mScrollX, instance);
                        } else {//向右滑
                            instance = -mScrollX % layoutWidth;
                            rightDirectionScroll(velocityX, mScrollX, instance);
                        }
                    }
                    invalidate();
                    //抬起时检测是否需要重绘界面
                    if (exchangeCount != downExchangeCount)
                        bannerChangeEvent(exchangeCount);
                    break;
                default:
                    break;
            }
            return true;
        }

        /**
         * 手动向右滑动
         *
         * @param velocityX 抬起时的x方向速度
         * @param mScrollX  抬起时已划过的x方向距离
         * @param instance  是指滑动过得距离
         */
        private void rightDirectionScroll(float velocityX, int mScrollX, int instance) {
            if (instance > layoutWidth / 2)
                scroller.startScroll(mScrollX, 0, -(layoutWidth - instance), 0, scrollDuration);
            else {
                if (minVelocity == Math.abs(velocityX))
                    scroller.startScroll(mScrollX, 0, -(layoutWidth - instance), 0, scrollDuration);
                else
                    scroller.startScroll(mScrollX, 0, instance, 0, scrollDuration);
            }
        }

        /**
         * 手动向左滑动
         *
         * @param velocityX 抬起时的x方向速度
         * @param mScrollX  抬起时已划过的x方向距离
         * @param instance  是指滑动过得距离
         */
        private void leftDirectionScroll(float velocityX, int mScrollX, int instance) {
            if (instance > layoutWidth / 2)
                scroller.startScroll(mScrollX, 0, layoutWidth - instance, 0, scrollDuration);
            else {
                if (minVelocity == Math.abs(velocityX))
                    scroller.startScroll(mScrollX, 0, layoutWidth - instance, 0, scrollDuration);
                else
                    scroller.startScroll(mScrollX, 0, -instance, 0, scrollDuration);

            }
        }

        /**
         * 获取点击的子view
         *
         * @param x       点击x坐标
         * @param y       点击y坐标
         * @param scrollX 横向滚动的距离
         * @param scrolly 纵向滚动的距离
         * @return 点击的子view
         */
        private View findTopChildUnder(int x, int y, int scrollX, int scrolly) {
            int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight()
                        && y + scrolly >= child.getTop() && y + scrolly < child.getBottom()) {
                    return child;
                }
            }
            return null;
        }

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == LOOP_MESSAGE) {
                    scroller.startScroll(getScrollX(), 0, layoutWidth, 0, scrollDuration);
                    invalidate();
                    startLoop();
                }
            }
        };

        private void startLoop() {
            handler.sendEmptyMessageDelayed(LOOP_MESSAGE, DELAY_TIME);
        }

        private void stopLoop() {
            handler.removeMessages(LOOP_MESSAGE);
        }
    }
}
