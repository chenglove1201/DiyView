package com.icandemy.diyview.loopbanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
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
                    getHeight());
            int swap = childViewOrder.removeLast();
            childViewOrder.addFirst(swap);
            exchangeCount--;
        }
        if (scrollX > (layoutWidth / 2 + layoutWidth * exchangeCount)) {
            Log.d("LoopBanner", "向左滑动，第一个子view放置最后");
            getChildAt(childViewOrder.get(0)).layout(getChildAt(childViewOrder.get(2)).getRight(),
                    0,
                    getChildAt(childViewOrder.get(2)).getRight() + layoutWidth,
                    getHeight());
            int swap = childViewOrder.removeFirst();
            childViewOrder.addLast(swap);
            exchangeCount++;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int scrollX = getScrollX();
                startX = (int) event.getX();
                startScrollX = scrollX;
                if (scroller.computeScrollOffset()) {
                    scroller.abortAnimation();
                }
                loop = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                scrollTo(-(currentX - startX) + startScrollX, 0);
                break;
            case MotionEvent.ACTION_UP:
                loop = true;
                int mScrollX = getScrollX();
                int instance = mScrollX % layoutWidth;
                if (Math.abs(instance) > getWidth() / 2) {
                    if (instance > 0) {//释放后向左滑动
                        Log.d("LoopBanner", "手指释放，向左滑动");
                        scroller.startScroll(mScrollX, 0, layoutWidth - instance, 0);
                    } else {//释放后向右滑动
                        Log.d("LoopBanner", "手指释放，向右滑动");
                        scroller.startScroll(mScrollX, 0, -instance - layoutWidth, 0);
                    }
                } else {
                    Log.d("LoopBanner", "手指释放，恢复向右滑动");
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

    /**
     * 是否自动循环
     */
    private boolean loop = true;

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
