# Android自定义控件
LoopBanner（轮播图控件）、TabLayout（圆角选择器的tablayout）、WheelView（日期选择器滚轮控件）

## LoopBanner
超精简自定义轮播图，实现无限循环滑动。（最少需要三张banner图片）

### 方法
|方法|描述
|---|---|
|setImageLoader(Object[] images, ImageLoader imageLoader)| 核心方法，设置图片加载器|无

### Attributes属
|attr|format|describe
|---|---|---|
|indication_margin| dimension|指示器间隔
|indication_diameter| dimension|指示器直径
|scroll_uration| integer|轮播滚动用时
|indication_normal| color|指示器未选择时颜色
|indication_selected| color|指示器选择时颜色

### 使用方法

#### Step 1.添加权限到 AndroidManifest.xml
```
<!-- 需要从网络获取图片 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 需要从本地加载图片 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

#### Step 2.在布局文件中添加LoopBanner
```
<com.cheng.diyview.loopbanner.LoopBanner
        android:id="@+id/loopBanner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```

#### Step 3.设置图片加载
```
loopBanner.setImageLoader(images, new LoopBanner.ImageLoader() {
            @Override
            public void loaderImage(Context context, ImageView imageView, Object source) {
                //使用Glide
                Glide.with(context).load(source).placeholder(R.mipmap.placeholder).into(imageView);

                //或者使用通用方法
                imageView.setImageResource((int) source);
            }
        });
```

#### 感谢
- [banner](https://github.com/youth5201314/banner)

## TabLayout

## WheelView