package com.cheng.diyview;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenglib.view.refresh.RefreshRecycleView;
import com.chenglib.view.refresh.RefreshRecycleViewAdapter;

public class RefreshRecycleViewActivity extends AppCompatActivity implements RefreshRecycleView.onPullRefreshListener {
    private RefreshRecycleView rfRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_recycle_view);

        rfRefresh = findViewById(R.id.rrv_content);
        rfRefresh.setOnPullRefreshListener(this);
        RecyclerView rvData = rfRefresh.getRecycleView();
        if (rvData == null) {
            throw new RuntimeException("RecycleView is null");
        }
        rvData.setLayoutManager(new LinearLayoutManager(this));
        RefreshRecycleViewAdapter recycleViewRefreshAdapter = new RefreshRecycleViewAdapter(this, rfRefresh) {
            @Override
            public int getItemCounts() {
                return 50;
            }

            @Override
            public RecyclerView.ViewHolder newViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == 1) {
                    return new TextViewHolder(LayoutInflater.from(RefreshRecycleViewActivity.this).inflate(R.layout.rrv_item_text, parent, false));
                } else if (viewType == 2) {
                    return new ImageViewHolder(LayoutInflater.from(RefreshRecycleViewActivity.this).inflate(R.layout.rrv_item_image, parent, false));
                }
                return null;
            }

            @Override
            public int itemViewType(int position) {
                if (position % 2 == 0) {
                    return 1;
                } else {
                    return 2;
                }
            }

            @Override
            public void setViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (position % 2 == 0) {
                    ((TextViewHolder) holder).tvTitle.setText("第" + position + "章");
                    ((TextViewHolder) holder).tvIntroduce.setText(position + "");
                } else {
                    ((ImageViewHolder) holder).ivPhoto.setImageResource(R.mipmap.ic_launcher);
                }
            }

            class ImageViewHolder extends RecyclerView.ViewHolder {
                ImageView ivPhoto;

                public ImageViewHolder(View itemView) {
                    super(itemView);
                    ivPhoto = itemView.findViewById(R.id.iv_photo);
                }
            }

            class TextViewHolder extends RecyclerView.ViewHolder {
                TextView tvTitle, tvIntroduce;

                public TextViewHolder(View itemView) {
                    super(itemView);
                    tvIntroduce = itemView.findViewById(R.id.tv_introduce);
                    tvTitle = itemView.findViewById(R.id.tv_title);
                }
            }
        };

        rvData.setAdapter(recycleViewRefreshAdapter);
        rfRefresh.setAdapter(recycleViewRefreshAdapter);

    }

    @Override
    public void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RefreshRecycleViewActivity.this, "刷新了一条数据", Toast.LENGTH_SHORT).show();
                rfRefresh.finishRefresh();
            }
        }, 3000);
    }

    @Override
    public void loadMore() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RefreshRecycleViewActivity.this, "加载了一条数据", Toast.LENGTH_SHORT).show();
                rfRefresh.finishLoadMore();
            }
        }, 3000);
    }

    public void driveRefresh(View view) {
        rfRefresh.refresh();
    }
}
