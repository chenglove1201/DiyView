package com.icandemy.diyview.loopbanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
    private ImageView imageView0, imageView1, imageView2;
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

        imageView0 = new ImageView(context);
        imageView0.setBackgroundColor(Color.RED);
        imageView0.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.a0001));
        imageView1 = new ImageView(context);
        imageView1.setBackgroundColor(Color.YELLOW);
        imageView1.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.a0002));
        imageView2 = new ImageView(context);
        imageView2.setBackgroundColor(Color.BLUE);
        imageView2.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.a0003));

        addView(imageView0);
        addView(imageView1);
        addView(imageView2);

        childViewOrder.add(0);
        childViewOrder.add(1);
        childViewOrder.add(2);


//        viewDrag();
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

    private void viewDrag() {
        scrollTo(300, 0);
        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            /**
             * 手指点击的imageview
             */
            int position;
            /**
             * 除手指点击的其他两个imageview
             */
            int other1, other2;
            /**
             * 处于最左侧的imageview的标号
             */
            int pos = 0;
            boolean autoScroll;


            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                autoScroll = false;
                if (child == getChildAt(0)) {
                    position = 0;
                    other1 = 1;
                    other2 = 2;
                } else if (child == getChildAt(1)) {
                    position = 1;
                    other1 = 2;
                    other2 = 0;
                } else {
                    position = 2;
                    other1 = 0;
                    other2 = 1;
                }

                return true;
            }

            int finalX;

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
//                Log.i("jfowejowjeg", getScrollX() + ".." + left + ".." + changedView.getScaleX() + ".." + changedView.getLeft() + ".." +
//                        changedView.getX());

                finalX = left;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                if (pos == 0) {
                    Log.i("Jfoiwejgwoe", "11111111111.." + position + ".." + other1 + ".." + other2);
                    if (position == 0) {
                        getChildAt(other1).layout(left + layoutWidth,//1
                                0,
                                left + layoutWidth * 2,
                                getHeight());
                        getChildAt(other2).layout(left + layoutWidth * 2,//2
                                0,
                                left + layoutWidth * 3,
                                getHeight());
                    } else if (position == 1) {
                        getChildAt(other1).layout(left + layoutWidth,//2
                                0,
                                left + layoutWidth * 2,
                                getHeight());
                        getChildAt(other2).layout(left - layoutWidth,//0
                                0,
                                left,
                                getHeight());
                    } else {
                        getChildAt(other1).layout(left - layoutWidth * 2,//0
                                0,
                                left - layoutWidth,
                                getHeight());
                        getChildAt(other2).layout(left - layoutWidth,//1
                                0,
                                left,
                                getHeight());
                    }
//                    getChildAt(other1).layout(left + layoutWidth * (other1 - position),
//                            0,
//                            left + layoutWidth * (other1 - position) + layoutWidth,
//                            getHeight());
//                    getChildAt(other2).layout(left + layoutWidth * (other2 - position),
//                            0,
//                            left + layoutWidth * (other2 - position) + layoutWidth,
//                            getHeight());
                } else if (pos == 1) {
                    Log.i("Jfoiwejgwoe", "22222222222222");
                    if (position == 0) {
                        getChildAt(other1).layout(left - layoutWidth * 2,//1
                                0,
                                left - layoutWidth,
                                getHeight());
                        getChildAt(other2).layout(left - layoutWidth,//2
                                0,
                                left,
                                getHeight());
                    } else if (position == 1) {
                        getChildAt(other1).layout(left + layoutWidth,//2
                                0,
                                left + layoutWidth * 2,
                                getHeight());
                        getChildAt(other2).layout(left + layoutWidth * 2,//0
                                0,
                                left + layoutWidth * 3,
                                getHeight());
                    } else {
                        getChildAt(other1).layout(left + layoutWidth,//0
                                0,
                                left + layoutWidth * 2,
                                getHeight());
                        getChildAt(other2).layout(left - layoutWidth,//1
                                0,
                                left,
                                getHeight());
                    }
                } else {
                    Log.i("Jfoiwejgwoe", "33333333333333");
                    if (position == 0) {
                        getChildAt(other1).layout(left + layoutWidth,
                                0,
                                left + layoutWidth * 2,
                                getHeight());
                        getChildAt(other2).layout(left - layoutWidth,
                                0,
                                left,
                                getHeight());
                    } else if (position == 1) {
                        getChildAt(other1).layout(left - layoutWidth * 2,
                                0,
                                left - layoutWidth,
                                getHeight());
                        getChildAt(other2).layout(left - layoutWidth,
                                0,
                                left,
                                getHeight());
                    } else {
                        getChildAt(other1).layout(left + layoutWidth,
                                0,
                                left + layoutWidth * 2,
                                getHeight());
                        getChildAt(other2).layout(left + layoutWidth * 2,
                                0,
                                left + layoutWidth * 3,
                                getHeight());
                    }
                }


                /*
                 * 位置变换，
                 * 规则：监测不可见的imageview，当两个imageview平均显示在屏幕时，让不可见的imageview根据滑动方向
                 * 重新布局在最左侧或最右侧
                 */
//                for (int i = 0; i < getChildCount(); i++) {
//                    if (Math.abs(getChildAt(i).getLeft()) >= layoutWidth + layoutWidth / 2) {
//                        if (dx < 0) {
//                            if (i == 0)
//                                pos = 1;
//                            else if (i == 1)
//                                pos = 2;
//                            else
//                                pos = 0;
//                        } else
//                            pos = i;
//                        getChildAt(i).layout(-getChildAt(i).getLeft(),
//                                0,
//                                -getChildAt(i).getLeft() + layoutWidth,
//                                getHeight());
//                    }
//                }

                return left;
            }

            @Override
            public void onViewReleased(final View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
//                Log.i("Jofwejogwe",releasedChild.getLeft()+"");
//                Log.i("jfowgowe", getScrollX() + ".." + finalX + ".." + (finalX - getScrollX()));
                int childLeft = releasedChild.getLeft();
                int[] locationOnScreen = new int[2];
                releasedChild.getLocationOnScreen(locationOnScreen);
                Log.i("jofwjgwegweg", getScrollX() + ".." + childLeft + ".." + releasedChild.getX() + ".." + releasedChild.getLeft() + ".." + locationOnScreen[0] + ".." + locationOnScreen[1]);

//                scroller.startScroll(getScrollX(), 0, childLeft > 0 ? childLeft - getScrollX() : layoutWidth + childLeft - getScrollX(), 0);
//                invalidate();
//                scrollBy(100,0);
            }
        });
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    public void startScroll() {

//        Log.i("fjoiwejgwe...1",getScrollX()+"");
//        scrollTo(100,0);
//        Log.i("fjoiwejgwe...2",getScrollX()+"");
    }

    private int startX;
    private int scrollX;
    /**
     * 子view的排列顺序
     */
    private LinkedList<Integer> childViewOrder = new LinkedList<>();
    private int lastViewAt;
    /**
     * 手指滑动时上一个点坐标
     */
    private int lastMoveX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        viewDragHelper.processTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastMoveX = (int) event.getX();
                startX = (int) event.getX();
                scrollX = getScrollX();
                break;
            case MotionEvent.ACTION_MOVE:
                scrollTo(-((int) (event.getX() - startX) - scrollX), 0);
                int currentViewAt = Math.abs(getScrollX()) / getWidth();
                if (event.getX() - lastMoveX > 0) {//向右滑
                    if (currentViewAt != lastViewAt) {
                        getChildAt(childViewOrder.get(2)).layout(getChildAt(childViewOrder.get(0)).getLeft() - layoutWidth,
                                0,
                                getChildAt(childViewOrder.get(0)).getLeft(),
                                getHeight());
                        lastViewAt = currentViewAt;
                        int swap = childViewOrder.removeLast();
                        childViewOrder.addFirst(swap);
                    }
                } else {//向左滑
                    if (currentViewAt != lastViewAt) {
                        getChildAt(childViewOrder.get(0)).layout(getChildAt(childViewOrder.get(2)).getRight(),
                                0,
                                getChildAt(childViewOrder.get(2)).getRight() + layoutWidth,
                                getHeight());
                        lastViewAt = currentViewAt;
                        int swap = childViewOrder.removeFirst();
                        childViewOrder.addLast(swap);
                    }
                }
                lastMoveX = (int) event.getX();
                break;
            case MotionEvent.ACTION_UP:
                int mScrollX = getScrollX();
                int mScrollY = getScrollY();
                View childView = findTopChildUnder((int) event.getX(), (int) event.getY(), mScrollX, mScrollY);
                int cdLeft = childView.getLeft();
                int instance = cdLeft - mScrollX;
                if (Math.abs(instance) > getWidth() / 2) {
                    if (instance > 0)//释放后向右滑动
                        scroller.startScroll(mScrollX, 0, cdLeft - mScrollX - layoutWidth, 0);
                    else//释放后向左滑动
                        scroller.startScroll(mScrollX, 0, cdLeft - mScrollX + layoutWidth, 0);
                } else
                    scroller.startScroll(mScrollX, 0, cdLeft - mScrollX, 0);
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
        Log.i("fowjgwegwe", x + ".." + y + ".." + scrollX + ".." + scrolly);
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight()
                    && y + scrolly >= child.getTop() && y + scrolly < child.getBottom()) {
                return child;
            } else {
                Log.i("fowjgwegwe", "fail..");
            }
        }
        return null;
    }
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return viewDragHelper.shouldInterceptTouchEvent(ev);
//    }
}
