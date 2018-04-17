package com.cheng.diyview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cheng.channelgridview.ChannelGridView;

import java.util.Arrays;

public class ChannelGridLayoutActivity extends AppCompatActivity implements ChannelGridView.OnChannelItemClickListener {
    ChannelGridView channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_grid_layout);

        channel = findViewById(R.id.cgv_channel);
        String[] myChannel = {"要闻", "视频", "新时代", "娱乐", "体育", "军事", "NBA", "国际", "科技", "财经", "汽车", "电影", "游戏", "独家", "房产",
                "图片", "时尚", "呼和浩特", "三打白骨精"};
        String[] recommendChannel = {"问答", "文化", "佛学", "股票", "动漫", "理财", "情感", "职场", "家居", "电竞", "数码", "星座", "教育", "美容", "电视剧",
                "搏击", "健康", "生活", "旅游"};
        channel.setMyChannel(myChannel);
        channel.setRecommendChannel(recommendChannel);
        channel.setOnChannelItemClickListener(this);
    }

    public void getMyChannel(View view) {
        String[] myChannel = channel.getMyChannel();
        Log.i("jfiowejiogjweg", Arrays.toString(myChannel));
    }

    public void getRecommendChannel(View view) {
        String[] recommendChannel = channel.getRecommendChannel();
        Log.i("jfiowejiogjweg", Arrays.toString(recommendChannel));
    }

    @Override
    public void channelItemClick(int itemId, String channel) {
        Log.i("jfiowejiogjioweg", itemId + ".." + channel);
    }
}
