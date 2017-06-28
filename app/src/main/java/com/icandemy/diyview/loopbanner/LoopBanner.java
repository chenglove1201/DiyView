package com.icandemy.diyview.loopbanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.util.LinkedList;

/**
 * Created by ICAN on 2017/6/12.
 */

public class LoopBanner extends LinearLayout {
    private BannerImage bannerImage;
    private int layoutWidth, layoutHeight;
    private Context context;

    public LoopBanner(Context context) {
        this(context, null);
    }

    public LoopBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;
        bannerImage = new BannerImage(context);
        addView(bannerImage);
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

        int childWidth = MeasureSpec.makeMeasureSpec(layoutWidth, MeasureSpec.EXACTLY);
        int childHeight = MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.EXACTLY);
        getChildAt(0).measure(childWidth, childHeight);

        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getChildAt(0).layout(0, 0, layoutWidth, layoutHeight);
    }

    public void setImageLoader(Object[] images, ImageLoader imageLoader) {
        this.images = images;
        this.imageLoader = imageLoader;

        bannerImage.setImage();
    }

    private ImageLoader imageLoader;
    private Object[] images;

    public interface ImageLoader {
        void loaderImage(Context context, ImageView imageView, Object source);
    }

    private class BannerImage extends ViewGroup {
        private Scroller scroller;

        private int showPosition = 0;

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

        private void init() {

        }

        public void setImage() {
            if (images.length > 3) {
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

                imageView0.setBackgroundColor(Color.RED);
                imageLoader.loaderImage(context, imageView0, images[images.length - 1]);
                imageView1.setBackgroundColor(Color.YELLOW);
                imageLoader.loaderImage(context, imageView1, images[showPosition]);
                imageView2.setBackgroundColor(Color.BLUE);
                imageLoader.loaderImage(context, imageView2, images[showPosition + 1]);

                //开启自动播放
                handler.sendEmptyMessageDelayed(1, 3000);
            } else
                Log.e("LoopBanner", "图片数量最小为3");
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }

        private boolean reLayout = true;

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

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }

        /**
         * 向左滑动时，每交换一次顺序，加一；向右滑动时，每交换一次顺序，减一
         */
        private int exchangeCount;

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
                    showPosition = images.length - 1;
                else
                    showPosition--;

                imageLoader.loaderImage(context, ((ImageView) getChildAt(childViewOrder.get(0))),
                        images[showPosition == 0 ? images.length - 1 : showPosition - 1]);
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
                if (showPosition == images.length)
                    showPosition = 0;

                imageLoader.loaderImage(context, ((ImageView) getChildAt(childViewOrder.get(2))),
                        images[showPosition == images.length - 1 ? 0 : showPosition + 1]);
            }
            if (scroller.computeScrollOffset()) {
                scrollTo(scroller.getCurrX(), scroller.getCurrY());
                invalidate();
            }
        }

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
        private VelocityTracker velocityTracker;

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
                    handler.removeMessages(1);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int currentX = (int) event.getX();
                    scrollTo(-(currentX - startX) + startScrollX, 0);
                    break;
                case MotionEvent.ACTION_UP:
                    int maxVelocity = 2500;
                    velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                    float velocityX = velocityTracker.getXVelocity();
                    //抬起时启动自动播放
                    handler.sendEmptyMessageDelayed(1, 3000);
                    int mScrollX = getScrollX();
                    int instance = mScrollX % layoutWidth;
                    if (Math.abs(instance) > getWidth() / 2) {
                        if (instance > 0) {//释放后向左滑动
                            if (velocityX == maxVelocity)
                                scroller.startScroll(mScrollX, 0, -instance - layoutWidth, 0);
                            else
                                scroller.startScroll(mScrollX, 0, layoutWidth - instance, 0);
                        } else {//释放后向右滑动
                            if (velocityX == -maxVelocity)
                                scroller.startScroll(mScrollX, 0, layoutWidth - instance, 0);
                            else
                                scroller.startScroll(mScrollX, 0, -instance - layoutWidth, 0);//这是右
                        }
                    } else {
                        if (velocityX == -maxVelocity)
                            scroller.startScroll(mScrollX, 0, layoutWidth - instance, 0);
                        else if (velocityX == maxVelocity)
                            scroller.startScroll(mScrollX, 0, -instance - layoutWidth, 0);
                        else
                            scroller.startScroll(mScrollX, 0, -instance, 0);
                    }
                    invalidate();
                    break;
                default:
                    break;
            }
            return true;
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

        public void startLoop() {

        }

        public void change() {

        }

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    scroller.startScroll(getScrollX(), 0, layoutWidth, 0);
                    invalidate();
                    handler.sendEmptyMessageDelayed(1, 3000);
                }
            }
        };
    }
}
