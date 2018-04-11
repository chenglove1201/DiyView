package com.chenglib.view.refresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by cheng on 2018/3/27.
 */

public abstract class RefreshRecycleViewAdapter extends RecyclerView.Adapter implements RefreshRecycleView.OnChangeTipListener, RefreshRecycleView.OnChangeHeadViewHeightListener {
    /**
     * 底部加载类型
     */
    private final int VIEW_TYPE_FOOT = 0xFFFF;
    /**
     * 顶部刷新类型
     */
    private final int VIEW_TYPE_HEAD = 0xAAAA;
    private Context mContext;
    private int itemCounts;
    private TextView tvFootTip;
    private TextView tvHeadTip;
    private View llHeadView;
    private View llFootView;
    private int refreshViewHeight;

    public abstract int getItemCounts();

    public abstract RecyclerView.ViewHolder newViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract int itemViewType(int position);

    public abstract void setViewHolder(@NonNull RecyclerView.ViewHolder holder, int position);

    public RefreshRecycleViewAdapter(Context context, RefreshRecycleView rfRefresh) {
        this.mContext = context;
        refreshViewHeight = dp2px(context, RefreshRecycleView.REFRESH_VIEW_HEIGHT_DP);
        rfRefresh.setOnChangeFootTipListener(this);
        rfRefresh.setOnChangeHeadViewHeightListener(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOT) {
            ViewGroup view = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.rrv_bottom_load, parent, false);
            ViewGroup childAt = (ViewGroup) view.getChildAt(0);
            ViewGroup.LayoutParams childLayoutParams = childAt.getLayoutParams();
            childLayoutParams.height = refreshViewHeight;
            //设置底部加载子视图高度
            childAt.setLayoutParams(childLayoutParams);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = refreshViewHeight;
            //设置底部加载父视图高度
            view.setLayoutParams(layoutParams);
            tvFootTip = view.findViewById(R.id.tv_foot_tip);
            llFootView = view.findViewById(R.id.ll_foot);
            LinearLayout llLoadMore = view.findViewById(R.id.ll_load_more);
            llLoadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onFootViewClickListener != null) {
                        onFootViewClickListener.loadMore();
                    }
                }
            });
            return new FootViewHolder(view);
        } else if (viewType == VIEW_TYPE_HEAD) {
            ViewGroup view = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.rrv_top_head, parent, false);
            ViewGroup childAt = (ViewGroup) view.getChildAt(0);
            ViewGroup.LayoutParams childLayoutParams = childAt.getLayoutParams();
            childLayoutParams.height = refreshViewHeight;
            childAt.setLayoutParams(childLayoutParams);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = 0;
            view.setLayoutParams(layoutParams);
            tvHeadTip = view.findViewById(R.id.tv_head_tip);
            llHeadView = view.findViewById(R.id.ll_head);
            return new HeadViewHolder(view);
        }
        return newViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position != 0 && position != itemCounts - 1) {
            setViewHolder(holder, position - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == itemCounts - 1) {
            return VIEW_TYPE_FOOT;
        } else if (position == 0) {
            return VIEW_TYPE_HEAD;
        }
        return itemViewType(position - 1);
    }

    @Override
    public int getItemCount() {
        itemCounts = getItemCounts() + 2;
        return itemCounts;
    }

    private class FootViewHolder extends RecyclerView.ViewHolder {

        private FootViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class HeadViewHolder extends RecyclerView.ViewHolder {

        private HeadViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void changeFootTips(String tips) {
        if (tvFootTip != null) {
            tvFootTip.setText(tips);
        }
    }

    @Override
    public void changeHeadTips(String tips) {
        if (tvHeadTip != null) {
            tvHeadTip.setText(tips);
        }
    }

    @Override
    public void changeHeadViewHeight(int headViewHeight) {
        if (llHeadView != null) {
            ViewGroup.LayoutParams layoutParams = llHeadView.getLayoutParams();
            if (headViewHeight < 0) {
                headViewHeight = 0;
            }
            layoutParams.height = headViewHeight;
            llHeadView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void changeFootViewHeight(int footViewHeight) {
        if (llFootView != null) {
            ViewGroup.LayoutParams layoutParams = llFootView.getLayoutParams();
            layoutParams.height = footViewHeight + refreshViewHeight;
            llFootView.setLayoutParams(layoutParams);
        }
    }

    private OnFootViewClickListener onFootViewClickListener;

    public void setOnFootViewClickListener(OnFootViewClickListener onFootViewClickListener) {
        this.onFootViewClickListener = onFootViewClickListener;
    }

    /**
     * dp转px
     */
    private int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }
}
