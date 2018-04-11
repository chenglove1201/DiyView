package com.cheng.diyview.channel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * Created by cheng on 2018/4/2.
 */

public class MyGridView extends GridView {
    public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            Log.i("jfiowejiogjwe", childAt.getLeft() + ".." + childAt.getTop() + ".." + childAt.getRight() + ".." + childAt.getBottom());
        }
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        Log.i("fawgweg","longggggggg");
    }
}
