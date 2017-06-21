package com.icandemy.diyview.loopbanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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
    private int layoutWidth;
    private ViewDragHelper viewDragHelper;
    private Context context;
    private Scroller scroller;

    public LoopBanner(Context context) {
        this(context, null);
    }

    public LoopBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;
        init();
    }

    private void init() {
        setWillNotDraw(false);

        scroller = new Scroller(context);

        ImageView imageView0 = new ImageView(context);
        imageView0.setBackgroundColor(Color.RED);
        imageView0.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.a0001));
        ImageView imageView1 = new ImageView(context);
        imageView1.setBackgroundColor(Color.YELLOW);
        imageView1.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.a0002));
        ImageView imageView2 = new ImageView(context);
        imageView2.setBackgroundColor(Color.BLUE);
        imageView2.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.a0003));

        addView(imageView0);
        addView(imageView1);
        addView(imageView2);

        childViewOrder.add(0);
        childViewOrder.add(1);
        childViewOrder.add(2);

        autoLoop();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = 0;
        layoutWidth = widthSize;

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0; i < getChildCount(); i++)
            heightSize = Math.max(heightSize, getChildAt(i).getMeasuredWidth());

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getChildAt(0).layout(-layoutWidth,
                0,
                0,
                getHeight());
        getChildAt(1).layout(0,
                0,
                layoutWidth,
                getHeight());
        getChildAt(2).layout(layoutWidth,
                0,
                layoutWidth * 2,
                getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        } else {
//            if (rebound) {
//                rebound = false;
//            } else {
            int scrollX = getScrollX();
            if (scrollX != 0 && scrollX > 0 && loop) {
                Log.i("bofwejgowe", "uiuiuiuiu");
                if (direction == null) {
//                        if (direction > 0) {//向左滑
                    getChildAt(childViewOrder.get(0)).layout(getChildAt(childViewOrder.get(2)).getRight(),
                            0,
                            getChildAt(childViewOrder.get(2)).getRight() + layoutWidth,
                            getHeight());
                    int swap = childViewOrder.removeFirst();
                    childViewOrder.addLast(swap);
                } else {
                    if (direction) {//向右滑
                        Log.i("bofwejgowe", "right..right..");
                        getChildAt(childViewOrder.get(2)).layout(getChildAt(childViewOrder.get(0)).getLeft() - layoutWidth,
                                0,
                                getChildAt(childViewOrder.get(0)).getLeft(),
                                getHeight());
                        int swap = childViewOrder.removeLast();
                        childViewOrder.addFirst(swap);
                    } else {//向左划
                        Log.i("bofwejgowe", "left..left..");
                        getChildAt(childViewOrder.get(0)).layout(getChildAt(childViewOrder.get(2)).getRight(),
                                0,
                                getChildAt(childViewOrder.get(2)).getRight() + layoutWidth,
                                getHeight());
                        int swap = childViewOrder.removeFirst();
                        childViewOrder.addLast(swap);
                    }
                    direction = null;
                }
//                        } else {//向右滑
//                            getChildAt(childViewOrder.get(2)).layout(getChildAt(childViewOrder.get(0)).getLeft() - layoutWidth,
//                                    0,
//                                    getChildAt(childViewOrder.get(0)).getLeft(),
//                                    getHeight());
//                            int swap = childViewOrder.removeLast();
//                            childViewOrder.addFirst(swap);
//                        }
            }
        }
        if (getScrollX() % layoutWidth == 0)
            currentScrollX = 0;
//        }
    }

    /**
     * 允许视图滚动
     */
    private float startX;
    private int startScrollX, lastScrollX, currentScrollX;
    /**
     * 子view的排列顺序
     */
    private LinkedList<Integer> childViewOrder = new LinkedList<>();
    private int lastViewAt;
    /**
     * 手指滑动时上一个点坐标
     */
    private float lastMoveX;

    private int scrollX;
    /**
     * 手动滚动
     */
//    private boolean manuslScroll;
    private Boolean direction;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scrollX = getScrollX();
                startX = lastMoveX = event.getX();
                startScrollX = scrollX;
                if (scroller.computeScrollOffset()) {
                    scroller.abortAnimation();
                    startScrollX = lastScrollX;
                    currentScrollX = scrollX % layoutWidth;
                }
                lastScrollX = startScrollX;
                loop = false;
                direction = null;
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = event.getX();
                scrollTo(-((int) (currentX - startX)) + startScrollX + currentScrollX, 0);
//                int currentViewAt = Math.abs(getScrollX()) / (getWidth() + 1);
//
//                if (currentX - lastMoveX > 0) {//向右滑
//                    Log.i("joiwfejgweg", "右右右");
//                    if (currentViewAt != lastViewAt) {
//                        Log.i("fwoehgowe....右", currentX + "...." + lastMoveX + "..." + currentViewAt + "...." + lastViewAt + "..." + getScrollX());
//                        getChildAt(childViewOrder.get(2)).layout(getChildAt(childViewOrder.get(0)).getLeft() - layoutWidth,
//                                0,
//                                getChildAt(childViewOrder.get(0)).getLeft(),
//                                getHeight());
//                        lastViewAt = currentViewAt;
//                        int swap = childViewOrder.removeLast();
//                        childViewOrder.addFirst(swap);
//                    }
//                } else if (currentX - lastMoveX < 0) {//向左滑
//                    Log.i("joiwfejgweg", "左左左左");
//                    if (currentViewAt != lastViewAt) {
//                        Log.i("fwoehgowe....左", currentX + "...." + lastMoveX + "..." + currentViewAt + "...." + lastViewAt + "..." + getScrollX());
//                        getChildAt(childViewOrder.get(0)).layout(getChildAt(childViewOrder.get(2)).getRight(),
//                                0,
//                                getChildAt(childViewOrder.get(2)).getRight() + layoutWidth,
//                                getHeight());
//                        lastViewAt = currentViewAt;
//                        int swap = childViewOrder.removeFirst();
//                        childViewOrder.addLast(swap);
//                    }
//                }
//                lastMoveX = currentX;
                break;
            case MotionEvent.ACTION_UP:
                loop = true;
                rebound = true;
                int mScrollX = getScrollX();
                int instance = mScrollX - startScrollX;
                if (Math.abs(instance) > getWidth() / 2) {
                    if (instance > 0) {//释放后向左滑动
                        direction = false;
                        scroller.startScroll(mScrollX, 0, layoutWidth - instance, 0);
                    } else {//释放后向右滑动
                        direction = true;
                        scroller.startScroll(mScrollX, 0, -instance - layoutWidth, 0);
                    }
                } else
                    scroller.startScroll(mScrollX, 0, -instance, 0);
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

    /**
     * 是否自动循环
     */
    private boolean loop = true;
    /**
     * 是否是松手后自动反弹
     */
    private boolean rebound;

    /**
     * 自动循环
     */
    private void autoLoop() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        sleep(3000);
                        if (loop) {
                            handler.sendEmptyMessage(1);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void startLoop() {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                scroller.startScroll(getScrollX(), 0, layoutWidth, 0);
                invalidate();
            }
        }
    };
}
