package com.chenglib.view.refresh;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by cheng on 2018/3/26.
 */

public class RefreshRecycleView extends LinearLayout implements NestedScrollingParent, OnFootViewClickListener {
    private int mTotalUnconsumed, mTotalUnconsumed2;
    private Context mContext;
    /**
     * 刷新后恢复原始位置所用时间
     */
    private final int ANIMATOR_TIME = 300;

    /**
     * 可刷新距离
     */
    private int refreshViewHeight;

    /**
     * 类型为下拉刷新
     */
    private final int PULL_REFRESH = 0x21;

    /**
     * 类型为上拉加载
     */
    private final int LOAD_MORE = 0x22;

    /**
     * 头、尾高度，dp
     */
    public final static int REFRESH_VIEW_HEIGHT_DP = 50;

    /**
     * 顶部下拉刷新开始的高度
     */
    private int startRefreshHeightHead;

    /**
     * 底部开始加载的高度
     */
    private int startLoadHeightFoot;

    /**
     * 是否正在刷新
     */
    private boolean refreshing;

    /**
     * 是否正在加载更多
     */
    private boolean loading;

    /**
     * RefreshView和RecycleView默认宽高
     */
    private final int VIEW_SIZE_DEFAULT = 500;

    private RecyclerView recyclerView;

    /**
     * 无操作
     */
    private final int DRAG_ACTION_NULL = 0x00;

    /**
     * 下拉
     */
    private final int DRAG_ACTION_PULL_DOWN = 0x01;

    /**
     * 下拉返回
     */
    private final int DRAG_ACTION_PULL_DOWN_BACK = 0x02;

    /**
     * 上拉
     */
    private final int DRAG_ACTION_PULL_UP = 0x03;

    /**
     * 上拉返回
     */
    private final int DRAG_ACTION_PULL_UP_BACK = 0x04;

    private int dragAction = DRAG_ACTION_NULL;

    public RefreshRecycleView(Context context) {
        this(context, null);
    }

    public RefreshRecycleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshRecycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        recyclerView = new RecyclerView(mContext, attrs);
        recyclerView.setVerticalScrollBarEnabled(true);
        addView(recyclerView);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        refreshViewHeight = dp2px(mContext, REFRESH_VIEW_HEIGHT_DP);
        startRefreshHeightHead = dp2px(mContext, REFRESH_VIEW_HEIGHT_DP + 20);
        startLoadHeightFoot = refreshViewHeight;
    }

    public RecyclerView getRecycleView() {
        return recyclerView;
    }

    /**
     * dp转px
     */
    private int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height, width;
        if (heightMode == MeasureSpec.AT_MOST) {
            height = VIEW_SIZE_DEFAULT;
        } else {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            width = VIEW_SIZE_DEFAULT;
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        getChildAt(0).measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        mTotalUnconsumed = 0;
        mTotalUnconsumed2 = 0;
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        if (mTotalUnconsumed > 0 && !refreshing) {
            release();
            mTotalUnconsumed = 0;
        }
        if (mTotalUnconsumed2 < 0) {
            release();
            mTotalUnconsumed2 = 0;
        }
    }

    /**
     * 手指释放
     */
    private void release() {
        //如果拖拽的距离大于可刷新距离
        if (dragAction == DRAG_ACTION_PULL_DOWN || dragAction == DRAG_ACTION_PULL_DOWN_BACK) {
            if (pullDownDistance >= startRefreshHeightHead) {
                if (onPullRefreshListener != null) {
                    refreshing = true;
                    adjustRefreshPosition(PULL_REFRESH);
                    onPullRefreshListener.refresh();
                    if (onChangeTipListener != null) {
                        onChangeTipListener.changeHeadTips("正在刷新");
                    }
                } else {
                    cancelRefresh();
                }
            } else {
                cancelRefresh();
            }
        }
        if (dragAction == DRAG_ACTION_PULL_UP || dragAction == DRAG_ACTION_PULL_UP_BACK) {
            if (pullUpDistance >= startLoadHeightFoot) {
                if (onPullRefreshListener != null) {
                    loading = true;
                    adjustRefreshPosition(LOAD_MORE);
                    onPullRefreshListener.loadMore();
                    if (onChangeTipListener != null) {
                        onChangeTipListener.changeFootTips("正在加载");
                    }
                } else {
                    cancelLoadMore();
                }
            } else {
                cancelLoadMore();
            }
        }
    }

    /**
     * 调整刷新位置
     */
    private void adjustRefreshPosition(int refreshType) {
        if (refreshType == PULL_REFRESH) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "pullDownDistance", pullDownDistance, refreshViewHeight);
            objectAnimator.setDuration(ANIMATOR_TIME);
            objectAnimator.start();
        } else if (refreshType == LOAD_MORE) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "pullUpDistance", pullUpDistance, 0);
            objectAnimator.setDuration(ANIMATOR_TIME);
            objectAnimator.start();
        }
    }

    /**
     * 完成刷新
     */
    public void finishRefresh() {
        stopRefresh();
        if (onChangeTipListener != null) {
            onChangeTipListener.changeHeadTips("刷新完成");
        }
    }

    private void cancelRefresh() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "pullDownDistance", pullDownDistance, 0);
        objectAnimator.setDuration(ANIMATOR_TIME);
        objectAnimator.start();
    }

    /**
     * 停止刷新
     */
    private void stopRefresh() {
        refreshing = false;
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "pullDownDistance", pullDownDistance, 0);
        objectAnimator.setDuration(ANIMATOR_TIME);
        objectAnimator.start();
    }

    /**
     * 完成加载
     */
    public void finishLoadMore() {
        stopLoadMore();
        if (onChangeTipListener != null) {
            onChangeTipListener.changeFootTips("加载完成");
        }
    }

    private void cancelLoadMore() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "pullUpDistance", pullUpDistance, 0);
        objectAnimator.setDuration(ANIMATOR_TIME);
        objectAnimator.start();
    }

    /**
     * 停止加载更多
     */
    private void stopLoadMore() {
        loading = false;
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "pullUpDistance", pullUpDistance, 0);
        objectAnimator.setDuration(ANIMATOR_TIME);
        objectAnimator.start();
    }

    /**
     * 是否正在刷新
     */
    public boolean isRefreshing() {
        return refreshing;
    }

    /**
     * 是否正在加载更多
     */
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed) {
        if (dyUnconsumed < 0 && !refreshing) {
            mTotalUnconsumed += -dyUnconsumed;
            onPullDown(dyUnconsumed);
        }
        if (dyUnconsumed > 0 && !loading) {
            mTotalUnconsumed2 += -dyUnconsumed;
            onPullUp(dyUnconsumed);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - mTotalUnconsumed;
                mTotalUnconsumed = 0;
                finishParentDrag();
            } else {
                mTotalUnconsumed -= dy;
                consumed[1] = dy;
            }
            onPullDownBack(dy);
        }
        if (dy < 0 && mTotalUnconsumed2 < 0) {
            if (dy < mTotalUnconsumed2) {
                consumed[1] = mTotalUnconsumed2 - dy;
                mTotalUnconsumed2 = 0;
                finishParentDrag();
            } else {
                mTotalUnconsumed2 -= dy;
                consumed[1] = dy;
            }
            onPullUpBack(dy);
        }
    }

    /**
     * 停止父view拖拽
     */
    private void finishParentDrag() {
        recyclerView.setVerticalScrollBarEnabled(true);
        dragAction = DRAG_ACTION_NULL;
        pullDownDistance = 0;
        pullUpDistance = 0;
    }

    private onPullRefreshListener onPullRefreshListener;

    public void setOnPullRefreshListener(onPullRefreshListener onPullRefreshListener) {
        this.onPullRefreshListener = onPullRefreshListener;
    }

    public interface onPullRefreshListener {
        /**
         * 下拉刷新
         */
        void refresh();

        /**
         * 上拉加载
         */
        void loadMore();

    }

    private OnChangeTipListener onChangeTipListener;

    public void setOnChangeFootTipListener(OnChangeTipListener onChangeTipListener) {
        this.onChangeTipListener = onChangeTipListener;
    }

    protected interface OnChangeTipListener {
        void changeFootTips(String tips);

        void changeHeadTips(String tips);
    }

    private OnChangeHeadViewHeightListener onChangeHeadViewHeightListener;

    public void setOnChangeHeadViewHeightListener(OnChangeHeadViewHeightListener onChangeHeadViewHeightListener) {
        this.onChangeHeadViewHeightListener = onChangeHeadViewHeightListener;
    }

    public interface OnChangeHeadViewHeightListener {
        void changeHeadViewHeight(int headViewHeight);

        void changeFootViewHeight(int footViewHeight);
    }

    private int pullDownDistance, pullUpDistance;

    private void onPullUp(int dy) {
        dragAction = DRAG_ACTION_PULL_UP;
        pullUpDistance += Math.abs(dy) / 2;
        changeTipsLoadMore();
        if (onChangeHeadViewHeightListener != null) {
            onChangeHeadViewHeightListener.changeFootViewHeight(pullUpDistance);
        }
    }

    private void onPullDownBack(int dy) {
        dragAction = DRAG_ACTION_PULL_DOWN_BACK;
        pullDownDistance -= Math.abs(dy) / 2;
        changeTipsRefresh();
        if (onChangeHeadViewHeightListener != null) {
            onChangeHeadViewHeightListener.changeHeadViewHeight(pullDownDistance);
        }
    }

    private void onPullDown(int dy) {
        dragAction = DRAG_ACTION_PULL_DOWN;
        pullDownDistance += Math.abs(dy) / 2;
        changeTipsRefresh();
        if (onChangeHeadViewHeightListener != null) {
            onChangeHeadViewHeightListener.changeHeadViewHeight(pullDownDistance);
        }
    }

    private void onPullUpBack(int dy) {
        dragAction = DRAG_ACTION_PULL_UP_BACK;
        pullUpDistance -= Math.abs(dy) / 2;
        changeTipsLoadMore();
        if (onChangeHeadViewHeightListener != null) {
            onChangeHeadViewHeightListener.changeFootViewHeight(pullUpDistance);
        }
    }

    private void changeTipsRefresh() {
        if (pullDownDistance >= startRefreshHeightHead) {
            if (onChangeTipListener != null) {
                onChangeTipListener.changeHeadTips("释放刷新");
            }
        } else {
            if (onChangeTipListener != null) {
                onChangeTipListener.changeHeadTips("下拉刷新");
            }
        }
    }

    private void changeTipsLoadMore() {
        if (pullUpDistance >= startLoadHeightFoot) {
            if (onChangeTipListener != null) {
                onChangeTipListener.changeFootTips("释放加载");
            }
        } else {
            if (onChangeTipListener != null) {
                onChangeTipListener.changeFootTips("上拉加载");
            }
        }
    }

    private int getPullDownDistance() {
        return pullDownDistance;
    }

    private void setPullDownDistance(int pullDownDistance) {
        this.pullDownDistance = pullDownDistance;
        if (onChangeHeadViewHeightListener != null) {
            onChangeHeadViewHeightListener.changeHeadViewHeight(pullDownDistance);
        }
    }

    private int getPullUpDistance() {
        return pullUpDistance;
    }

    private void setPullUpDistance(int pullUpDistance) {
        this.pullUpDistance = pullUpDistance;
        if (onChangeHeadViewHeightListener != null) {
            onChangeHeadViewHeightListener.changeFootViewHeight(pullUpDistance);
        }
    }

    /**
     * 主动刷新
     */
    public void refresh() {
        if (!refreshing) {
            if (onPullRefreshListener != null) {
                recyclerView.scrollToPosition(0);
                refreshing = true;
                adjustRefreshPosition(PULL_REFRESH);
                onPullRefreshListener.refresh();
                if (onChangeTipListener != null) {
                    onChangeTipListener.changeHeadTips("正在刷新");
                }
            } else {
                cancelRefresh();
            }
        }
    }

    /**
     * 主动加载
     */
    @Override
    public void loadMore() {
        if (!loading) {
            if (onPullRefreshListener != null) {
                loading = true;
                adjustRefreshPosition(LOAD_MORE);
                onPullRefreshListener.loadMore();
                if (onChangeTipListener != null) {
                    onChangeTipListener.changeFootTips("正在加载");
                }
            } else {
                cancelLoadMore();
            }
        }
    }

    public void setAdapter(RefreshRecycleViewAdapter adapter) {
        adapter.setOnFootViewClickListener(this);
    }

}