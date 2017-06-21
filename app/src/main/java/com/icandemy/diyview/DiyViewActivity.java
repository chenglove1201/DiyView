package com.icandemy.diyview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.icandemy.diyview.loopbanner.LoopBanner;

public class DiyViewActivity extends AppCompatActivity {
    private LoopBanner loopBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy_view);

        loopBanner = (LoopBanner) findViewById(R.id.loopBanner);
    }

    public void scroll(View view) {
        loopBanner.startLoop();
    }
}
