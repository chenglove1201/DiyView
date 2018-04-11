package com.cheng.diyview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toTabLayout(View view) {
        startActivity(new Intent(this, TabLayoutActivity.class));
    }

    public void toLoopBanner(View view) {
    }

    public void toWheel(View view) {
    }

    public void toRefreshRecycleView(View view) {
        startActivity(new Intent(this, RefreshRecycleViewActivity.class));
    }
}
