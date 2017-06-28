package com.icandemy.diyview;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.icandemy.diyview.loopbanner.LoopBanner;

public class DiyViewActivity extends AppCompatActivity {
    private LoopBanner loopBanner;
    private Object[] images = new Object[]{R.mipmap.a0000, R.mipmap.a0001, R.mipmap.a0002, R.mipmap.a0003, R.mipmap.a0004};
    private static final String imageUrl = "https://www.icandemy.cn/image/0001.jpg";
    private static final String imageUrl2 = "https://www.icandemy.cn/image/0003.jpg";
    private static final String imageUrl3 = "https://www.icandemy.cn/image/0004.jpg";
    private static final String imageUrl4 = "https://www.icandemy.cn/image/0002.jpg";
    private static final String imageUrl5 = "https://www.icandemy.cn/image/0000.jpg";
    private Object[] imageUrls = new Object[]{imageUrl, imageUrl2, imageUrl3, imageUrl4, imageUrl5};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy_view);

        loopBanner = (LoopBanner) findViewById(R.id.loopBanner);

        loopBanner.setImageLoader(images, new LoopBanner.ImageLoader() {
            @Override
            public void loaderImage(Context context, ImageView imageView, Object source) {
                Glide.with(context).load(source).placeholder(R.mipmap.placeholder).into(imageView);
//                imageView.setImageResource((int) source);
            }
        });
    }

//    public void scroll(View view) {
//        loopBanner.startLoop();
//    }
//
//    public void change(View view) {
//        loopBanner.change();
//    }
}
