package com.cheng.diyview.channel;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheng.diyview.R;

/**
 * Created by cheng on 2018/3/30.
 * <p>
 * 频道网格布局
 */

public class ChannelGridView extends LinearLayout {
    private Channel myChannel, recommendChannel;
    private View view1, view2;
    private TextView view1Tip;

    public ChannelGridView(Context context) {
        this(context, null);
    }

    public ChannelGridView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChannelGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view1 = LayoutInflater.from(context).inflate(R.layout.cgl_my_channel, null);
        TextView view1Title = view1.findViewById(R.id.tv_title);
        view1Tip = view1.findViewById(R.id.tv_tip);
        view1Title.setText("已选频道");
        view1Tip.setText("按住拖拽频道");
        view1Tip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view1Tip.getText().toString().equals("完成")) {
                    myChannel.stopDrag();
                    view1Tip.setText("按住拖拽频道");
                }
            }
        });
        addView(view1);
        myChannel = new Channel(context, attrs, defStyleAttr);
        myChannel.setEnableDrag(true);
        addView(myChannel);
        view2 = LayoutInflater.from(context).inflate(R.layout.cgl_my_channel, null);
        TextView view2Title = view2.findViewById(R.id.tv_title);
        final TextView view2Tip = view2.findViewById(R.id.tv_tip);
        view2Title.setText("推荐频道");
        view2Tip.setText("点击添加频道");
        addView(view2);
        recommendChannel = new Channel(context, attrs, defStyleAttr);
        recommendChannel.setEnableDrag(false);
        addView(recommendChannel);
        setOrientation(VERTICAL);
    }

    /**
     * 添加频道
     */
    private void clickAddChannel(View v) {
        myChannel.addChannel(v);
    }

    /**
     * 删除我的频道
     *
     * @param v
     */
    private void clickRecycleChannel(View v) {
        recommendChannel.recycleChannel(v);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        myChannel.measure(widthMeasureSpec, heightMeasureSpec);
        recommendChannel.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(view1.getMeasuredWidth() + myChannel.getMeasuredWidth() + view2.getMeasuredWidth() + recommendChannel.getMeasuredWidth(),
                view1.getMeasuredHeight() + myChannel.getMeasuredHeight() + view2.getMeasuredHeight() + recommendChannel.getMeasuredHeight());
    }

    private class Channel extends ViewGroup implements View.OnTouchListener, OnClickListener, OnLongClickListener {
        private Context mContext;

        /**
         * 左右间隔
         */
        private int horizontalSpacing;

        /**
         * 上下间隔
         */
        private int verticalSpacing;

        /**
         * 列数
         */
        private int channelColumn;

        /**
         * 频道宽
         */
        private int channelWidth;

        /**
         * 频道高
         */
        private int channelHeight;

        private int channelLeftPadding;

        private int channelRightPadding;

        private int channelTopPadding;

        private int channelBottomPadding;

        private String[] content = {"要闻", "视频", "新时代", "娱乐", "体育", "军事", "NBA", "国际", "科技"};

//        private String[] content = {"要闻", "视频", "新时代", "娱乐", "体育", "军事", "NBA", "国际", "科技", "财经", "汽车", "电影", "游戏", "独家", "房产",
//                "图片", "时尚", "呼和浩特", "三打白骨精"};

        /**
         * 频道坐标
         */
        private SparseArray<PointF> channelPoints;

        /**
         * 频道集合
         */
        private SparseArray<View> channelViews;

        private final int DURATION_TIME = 2000;

        public Channel(Context context) {
            this(context, null);
        }

        public Channel(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public Channel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.mContext = context;
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Channel);
            verticalSpacing = (int) typedArray.getDimension(R.styleable.Channel_verticalSpacing, 30);
            channelWidth = (int) typedArray.getDimension(R.styleable.Channel_channelWidth, 0);
            channelHeight = (int) typedArray.getDimension(R.styleable.Channel_channelHeight, 0);
            channelColumn = typedArray.getInteger(R.styleable.Channel_channelColumn, 4);
            channelLeftPadding = (int) typedArray.getDimension(R.styleable.Channel_channelLeftPadding, 0);
            channelRightPadding = (int) typedArray.getDimension(R.styleable.Channel_channelRightPadding, 0);
            channelTopPadding = (int) typedArray.getDimension(R.styleable.Channel_channelTopPadding, 0);
            channelBottomPadding = (int) typedArray.getDimension(R.styleable.Channel_channelBottomPadding, 0);
            typedArray.recycle();
            init();
        }

        private void init() {
            if (channelColumn <= 0) {
                channelColumn = 1;
            }
            if (channelLeftPadding <= 0) {
                channelLeftPadding = 0;
            }
            if (channelTopPadding <= 0) {
                channelTopPadding = 0;
            }
            if (channelRightPadding <= 0) {
                channelRightPadding = 0;
            }
            if (channelBottomPadding <= 0) {
                channelBottomPadding = 0;
            }
            channelPoints = new SparseArray<>(content.length * 2);
            channelViews = new SparseArray<>(content.length * 2);
            for (int i = 0; i < content.length; i++) {
                TextView textView = new TextView(mContext);
                textView.setText(content[i]);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundResource(R.drawable.bg_channel_tag_normal);
                textView.setOnClickListener(this);
                addView(textView);
                channelViews.put(i, textView);
            }
        }

        /**
         * 设置是否能拖拽
         */
        public void setEnableDrag(boolean isEnableDrag) {
            if (isEnableDrag) {
                channelClickType = NORMAL;
                for (int i = 0; i < channelViews.size(); i++) {
                    channelViews.get(i).setOnTouchListener(this);
                    channelViews.get(i).setOnLongClickListener(this);
                }
            } else {
                channelClickType = ADD;
            }
        }

        private boolean isLayout = true;

        private AnimatorSet animatorSet = new AnimatorSet();

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width, height;
            if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                width = MeasureSpec.getSize(widthMeasureSpec);
            } else {
                width = 500;
            }
            int childCount = channelViews.size();
            if (channelWidth <= 0) {
                measureChildren(widthMeasureSpec, heightMeasureSpec);
                for (int i = 0; i < childCount; i++) {
                    int channelMeasuredWidth = channelViews.get(i).getMeasuredWidth();
                    channelWidth = channelMeasuredWidth > channelWidth ? channelMeasuredWidth : channelWidth;
                }
            }
            if (channelHeight <= 0) {
                if (channelWidth > 0) {
                    measureChildren(widthMeasureSpec, heightMeasureSpec);
                }
                for (int i = 0; i < childCount; i++) {
                    int channelMeasuredHeight = channelViews.get(i).getMeasuredHeight();
                    channelHeight = channelMeasuredHeight > channelHeight ? channelMeasuredHeight : channelHeight;
                }
            }
            for (int i = 0; i < childCount; i++) {
                channelViews.get(i).measure(MeasureSpec.makeMeasureSpec(channelWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(channelHeight, MeasureSpec.EXACTLY));
            }
            if (channelColumn == 1) {
                horizontalSpacing = width - channelWidth - channelLeftPadding - channelRightPadding;
            } else {
                horizontalSpacing = (width - channelWidth * channelColumn - channelLeftPadding - channelRightPadding) / (channelColumn - 1);
            }
            horizontalSpacing = horizontalSpacing <= 0 ? 0 : horizontalSpacing;
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                height = MeasureSpec.getSize(heightMeasureSpec);
            } else {
                int rowCount = childCount % channelColumn == 0 ? childCount / channelColumn : childCount / channelColumn + 1;
                int spaceCount = childCount % channelColumn == 0 ? childCount / channelColumn - 1 : childCount / channelColumn;
                height = rowCount * channelHeight + verticalSpacing * spaceCount + channelTopPadding + channelBottomPadding;
            }
            if (!isDynamicAddHeight) {//不是动态的增加高度
                setMeasuredDimension(width, height);
            } else {//动态的增加高度
                setMeasuredDimension(width, dynamicHeight);
            }
        }

        /**
         * 是否增加频道
         */
        private boolean isChannelEditing;

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (isLayout) {
                for (int i = 0; i < channelViews.size(); i++) {
                    View childAt = channelViews.get(i);
//            switch (i % CHANNEL_COLUMN) {
//                case 0:
//                    childAt.layout(0, (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN), childWidth,
//                            (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN) + childHeight);
//                    break;
//                case 1:
//                    childAt.layout(childWidth + horizontalSpacing, (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN),
//                            childWidth * 2 + horizontalSpacing, (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN) + childHeight);
//                    break;
//                case 2:
//                    childAt.layout(childWidth * 2 + horizontalSpacing * 2, (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN),
//                            childWidth * 3 + horizontalSpacing * 2, (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN) + childHeight);
//                    break;
//                case 3:
//                    childAt.layout(childWidth * 3 + horizontalSpacing * 3, (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN),
//                            childWidth * 4 + horizontalSpacing * 3, (childHeight + verticalSpacing) * (i / CHANNEL_COLUMN) + childHeight);
//                    break;
//                default:
//                    break;
//            }
                    int flag = i % channelColumn;
                    childAt.layout(channelWidth * flag + horizontalSpacing * flag + channelLeftPadding,
                            (channelHeight + verticalSpacing) * (i / channelColumn) + channelTopPadding,
                            channelWidth * (flag + 1) + horizontalSpacing * flag + channelLeftPadding,
                            (channelHeight + verticalSpacing) * (i / channelColumn) + channelHeight + channelTopPadding);
                    channelPoints.put(i, new PointF(childAt.getX(), childAt.getY()));
                }
                isLayout = false;
            }
            if (isChannelEditing) {
                int childAt = channelViews.size() - 1;
                int flag = childAt % channelColumn;
                View childView = channelViews.get(childAt);
                childView.layout(channelWidth * flag + horizontalSpacing * flag + channelLeftPadding,
                        (channelHeight + verticalSpacing) * ((childAt) / channelColumn) + channelTopPadding,
                        channelWidth * (flag + 1) + horizontalSpacing * flag + channelLeftPadding,
                        (channelHeight + verticalSpacing) * ((childAt) / channelColumn) + channelHeight + channelTopPadding);
//                channelPoints.put(childAt, new PointF(childView.getX(), childView.getY()));
                isChannelEditing = false;
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = event.getRawX();
                downY = event.getRawY();
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE && isChannelLongClick) {
                channelDrag(v, event);
            }
            if (event.getAction() == MotionEvent.ACTION_UP && isChannelLongClick) {
                channelDragRelease(v);
            }
            return false;
        }

        /**
         * 频道普通点击
         */
        private final int NORMAL = 0X00;

        /**
         * 点击增加频道
         */
        private final int ADD = 0X01;

        /**
         * 点击删除频道
         */
        private final int DELETE = 0x02;

        private int channelClickType = NORMAL;

        @Override
        public void onClick(final View v) {
            if (channelClickType == NORMAL) {

            } else if (channelClickType == ADD) {
                //推荐频道中，要做相应的移除一个view后的过渡动画
                if (!animatorSet.isRunning()) {
                    //在myChannel需要操作的事情
                    clickAddChannel(v);
                    //在recommendChannel需要操作的事情
                    int vPosition = channelViews.indexOfValue(v);
                    animatorSet = new AnimatorSet();
                    ObjectAnimator[] objectAnimators = new ObjectAnimator[(channelViews.size() - vPosition - 1) * 2];
                    if (objectAnimators.length > 0) {
                        for (int i = vPosition; i < channelViews.size() - 1; i++) {
                            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(channelViews.get(i + 1), "X", channelPoints.get(i).x);
                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(channelViews.get(i + 1), "Y", channelPoints.get(i).y);
                            objectAnimators[2 * (i - vPosition)] = objectAnimator;
                            objectAnimators[2 * (i - vPosition) + 1] = objectAnimator1;
                            channelViews.put(i, channelViews.get(i + 1));
                        }
                        animatorSet.playTogether(objectAnimators);
                        animatorSet.setDuration(DURATION_TIME);
                        animatorSet.start();
                    }
                    channelViews.removeAt(channelViews.size() - 1);
                    channelPoints.removeAt(channelPoints.size() - 1);
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            removeView(v);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    if (channelViews.size() % channelColumn == 0) {
                        isDynamicAddHeight = true;
                        ValueAnimator dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() - channelHeight - verticalSpacing);
                        dynamicHeightAnimator.setDuration(DURATION_TIME);
                        dynamicHeightAnimator.start();
                        dynamicHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                dynamicHeight = (int) animation.getAnimatedValue();
                                requestLayout();
                            }
                        });
                        dynamicHeightAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                isDynamicAddHeight = false;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }
                }
            } else if (channelClickType == DELETE) {
                if (!animatorSet.isRunning()) {
                    clickRecycleChannel(v);
                    v.setVisibility(INVISIBLE);
                    animatorSet = new AnimatorSet();
                    int vPosition = channelViews.indexOfValue(v);
                    animatorSet = new AnimatorSet();
                    ObjectAnimator[] objectAnimators = new ObjectAnimator[(channelViews.size() - vPosition - 1) * 2];
                    if (objectAnimators.length > 0) {
                        for (int i = vPosition; i < channelViews.size() - 1; i++) {
                            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(channelViews.get(i + 1), "X", channelPoints.get(i).x);
                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(channelViews.get(i + 1), "Y", channelPoints.get(i).y);
                            objectAnimators[2 * (i - vPosition)] = objectAnimator;
                            objectAnimators[2 * (i - vPosition) + 1] = objectAnimator1;
                            channelViews.put(i, channelViews.get(i + 1));
                        }
                        animatorSet.playTogether(objectAnimators);
                        animatorSet.setDuration(DURATION_TIME);
                        animatorSet.start();
                    }
                    channelViews.removeAt(channelViews.size() - 1);
                    channelPoints.removeAt(channelPoints.size() - 1);
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            removeView(v);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    if (channelViews.size() % channelColumn == 0) {
                        isDynamicAddHeight = true;
                        ValueAnimator dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() - channelHeight - verticalSpacing);
                        dynamicHeightAnimator.setDuration(DURATION_TIME);
                        dynamicHeightAnimator.start();
                        dynamicHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                dynamicHeight = (int) animation.getAnimatedValue();
                                requestLayout();
                            }
                        });
                        dynamicHeightAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                isDynamicAddHeight = false;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (!animatorSet.isRunning()) {
                view1Tip.setText("完成");
                channelClickType = DELETE;
                isChannelLongClick = true;
                copyChannel = new TextView(mContext);
                copyChannel.setLayoutParams(new ViewGroup.LayoutParams(channelWidth, channelHeight));
                copyChannel.setText(((TextView) v).getText());
                copyChannel.setGravity(Gravity.CENTER);
                copyChannel.setBackgroundResource(R.drawable.bg_channel_tag_focused);
//                copyChannel.measure(MeasureSpec.makeMeasureSpec(channelWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(channelHeight, MeasureSpec.EXACTLY));
//                copyChannel.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                copyChannel.setX(v.getX());
                copyChannel.setY(v.getY() - getMeasuredHeight() - view2.getMeasuredHeight() - recommendChannel.getMeasuredHeight());
                ChannelGridView.this.addView(copyChannel);
                selectPosition = vPosition = channelViews.indexOfValue(v);
                selectChannelX = channelPoints.get(vPosition).x;
                selectChannelY = channelPoints.get(vPosition).y;
                for (int i = 0; i < channelViews.size(); i++) {
                    channelViews.get(i).setBackgroundResource(R.drawable.bg_channel_tag_selected);
                }
            }
            return true;
        }

        float downX, downY;
        float moveX, moveY;

        private boolean isChannelLongClick;

        /**
         * 点击频道时复制的频道
         * <p>防止频道拖动时出现在其他频道下方造成遮盖，复制出一个最顶层的view</p>
         */
        private TextView copyChannel;

        private float selectChannelX, selectChannelY;
        private int vPosition, selectPosition;

        /**
         * 频道拖动
         */
        private void channelDrag(View v, MotionEvent event) {
            moveX = event.getRawX();
            moveY = event.getRawY();
            copyChannel.setX(copyChannel.getX() + (moveX - downX));
            copyChannel.setY(copyChannel.getY() + (moveY - downY));
            v.setX(v.getX() + (moveX - downX));
            v.setY(v.getY() + (moveY - downY));
            downX = moveX;
            downY = moveY;

            for (int i = 0; i < channelViews.size(); i++) {
                if (vPosition != i) {
                    int x1 = (int) channelPoints.get(i).x;
                    int y1 = (int) channelPoints.get(i).y;
                    int sqrt = (int) Math.sqrt((v.getX() - x1) * (v.getX() - x1) + (v.getY() - y1) * (v.getY() - y1));
                    if (sqrt <= 100) {
                        if (!animatorSet.isRunning()) {
                            selectPosition = i;
                            selectChannelX = channelPoints.get(selectPosition).x;
                            selectChannelY = channelPoints.get(selectPosition).y;
                            animatorSet = new AnimatorSet();
                            ObjectAnimator[] objectAnimators = new ObjectAnimator[Math.abs(vPosition - selectPosition) * 2];
                            for (int j = 0; j < Math.abs(vPosition - selectPosition); j++) {
                                //频道往前排列
                                if (vPosition >= selectPosition) {
                                    objectAnimators[2 * j] = ObjectAnimator.ofFloat(channelViews.get(vPosition - j - 1), "X", channelPoints.get(vPosition - j).x);
                                    objectAnimators[2 * j + 1] = ObjectAnimator.ofFloat(channelViews.get(vPosition - j - 1), "Y", channelPoints.get(vPosition - j).y);
                                    channelViews.put(vPosition - j, channelViews.get(vPosition - j - 1));
                                } else {//频道往后排列
                                    objectAnimators[2 * j] = ObjectAnimator.ofFloat(channelViews.get(vPosition + j + 1), "X", channelPoints.get(vPosition + j).x);
                                    objectAnimators[2 * j + 1] = ObjectAnimator.ofFloat(channelViews.get(vPosition + j + 1), "Y", channelPoints.get(vPosition + j).y);
                                    channelViews.put(vPosition + j, channelViews.get(vPosition + j + 1));
                                }
                            }
//                            if (objectAnimators.length > 0) {
                            vPosition = selectPosition;
                            animatorSet.playTogether(objectAnimators);
                            animatorSet.setDuration(DURATION_TIME);
                            animatorSet.start();
//                            }
                        }
                    }
                }
            }
        }

        /**
         * 频道拖动释放
         *
         * @param v
         */
        private void channelDragRelease(View v) {
            isChannelLongClick = false;
            channelViews.put(selectPosition, v);
            v.animate().x(selectChannelX).y(selectChannelY).setDuration(DURATION_TIME);
            ViewPropertyAnimator animate = copyChannel.animate();
            animate.x(selectChannelX).y(selectChannelY + view1.getMeasuredHeight()).setDuration(DURATION_TIME);
            animate.setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ChannelGridView.this.removeView(copyChannel);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        /**
         * 完成拖拽
         */
        public void stopDrag() {
            channelClickType = NORMAL;
            for (int i = 0; i < channelViews.size(); i++) {
                channelViews.get(i).setBackgroundResource(R.drawable.bg_channel_tag_normal);
            }
        }

        /**
         * 是否动态的增加高度
         * <p>当添加频道时，如果新增一行，动态的增加高度</p>
         */
        private boolean isDynamicAddHeight;

        private int dynamicHeight;

        /**
         * 开始时动态高度
         */
        private int startDynamicHeight;

        /**
         * 结束时动态高度
         */
        private int endDynamicHeight;

        public void addChannel(final View v) {
            final TextView newAddChannel = copyChannel(v);
            newAddChannel.setVisibility(INVISIBLE);
            if (channelClickType == DELETE) {
                newAddChannel.setBackgroundResource(R.drawable.bg_channel_tag_selected);
            } else {
                newAddChannel.setBackgroundResource(R.drawable.bg_channel_tag_normal);
            }
            isChannelEditing = true;
            channelViews.put(channelViews.size(), newAddChannel);
            if (channelViews.size() % channelColumn == 1) {
                isDynamicAddHeight = true;
            }
            startDynamicHeight = getMeasuredHeight();
            endDynamicHeight = startDynamicHeight + channelHeight + channelBottomPadding + verticalSpacing;
            addView(newAddChannel);
            if (channelPoints.size() % channelColumn == 0) {
                channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(0).x,
                        channelPoints.get(channelPoints.size() - 1).y + verticalSpacing + channelHeight));
            } else {
                channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(channelPoints.size() - 1).x + channelWidth + horizontalSpacing,
                        channelPoints.get(channelPoints.size() - 1).y));
            }
            if (isDynamicAddHeight) {
                ValueAnimator dynamicHeightAnimator = ObjectAnimator.ofInt(startDynamicHeight, endDynamicHeight);
                dynamicHeightAnimator.setDuration(DURATION_TIME);
                dynamicHeightAnimator.start();
                dynamicHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        dynamicHeight = (int) animation.getAnimatedValue();
                        requestLayout();
                    }
                });
                dynamicHeightAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isDynamicAddHeight = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
            //当增加一个频道时，在父view中复制一个做过渡动画
            final TextView copyOutChannel = copyChannel(v);
            copyOutChannel.setBackgroundResource(R.drawable.bg_channel_tag_normal);
            copyOutChannel.setLayoutParams(new LayoutParams(channelWidth, channelHeight));
            copyOutChannel.setX(v.getX());
            copyOutChannel.setY(v.getY() + view1.getMeasuredHeight() + myChannel.getMeasuredHeight() + view2.getMeasuredHeight());
            ChannelGridView.this.addView(copyOutChannel);
            ViewPropertyAnimator animate = copyOutChannel.animate();
            if ((channelPoints.size() - 1) % channelColumn == 0) {
                animate.x(channelPoints.get(0).x)
                        .y(channelPoints.get(channelPoints.size() - 1).y + view1.getMeasuredHeight())
                        .setDuration(DURATION_TIME);
            } else {
                animate.x(channelPoints.get(channelPoints.size() - 1).x)
                        .y(channelPoints.get(channelPoints.size() - 1).y + view1.getMeasuredHeight())
                        .setDuration(DURATION_TIME);

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animate.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if ((float) animation.getAnimatedValue() > 0) {
                            v.setVisibility(INVISIBLE);
                        }
                    }
                });
            }
            animate.setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                        v.setVisibility(INVISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    newAddChannel.setVisibility(VISIBLE);
                    ChannelGridView.this.removeView(copyOutChannel);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        /**
         * 复制频道
         */
        private TextView copyChannel(View v) {
            TextView textView = new TextView(mContext);
            textView.setText(((TextView) v).getText());
            textView.setGravity(Gravity.CENTER);
            textView.setOnTouchListener(this);
            textView.setOnLongClickListener(this);
            textView.setOnClickListener(this);
            return textView;
        }

        /**
         * 删除我的频道
         *
         * @param v
         */
        public void recycleChannel(final View v) {
//            final TextView newAddChannel = copyChannel(v);
//            newAddChannel.setVisibility(INVISIBLE);
//            if (channelClickType == DELETE) {
//                newAddChannel.setBackgroundResource(R.drawable.bg_channel_tag_selected);
//            } else {
//                newAddChannel.setBackgroundResource(R.drawable.bg_channel_tag_normal);
//            }
//            isChannelEditing = true;
//            channelViews.put(channelViews.size(), newAddChannel);
//            if (channelViews.size() % channelColumn == 1) {
//                isDynamicAddHeight = true;
//            }
//            startDynamicHeight = getMeasuredHeight();
//            endDynamicHeight = startDynamicHeight + channelHeight + channelBottomPadding + verticalSpacing;
//            addView(newAddChannel);
//            if (channelPoints.size() % channelColumn == 0) {
//                channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(0).x,
//                        channelPoints.get(channelPoints.size() - 1).y + verticalSpacing + channelHeight));
//            } else {
//                channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(channelPoints.size() - 1).x + channelWidth + horizontalSpacing,
//                        channelPoints.get(channelPoints.size() - 1).y));
//            }
//            if (isDynamicAddHeight) {
//                ValueAnimator dynamicHeightAnimator = ObjectAnimator.ofInt(startDynamicHeight, endDynamicHeight);
//                dynamicHeightAnimator.setDuration(DURATION_TIME);
//                dynamicHeightAnimator.start();
//                dynamicHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        dynamicHeight = (int) animation.getAnimatedValue();
//                        requestLayout();
//                    }
//                });
//                dynamicHeightAnimator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        isDynamicAddHeight = false;
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//            }
//            //当增加一个频道时，在父view中复制一个做过渡动画
//            final TextView copyOutChannel = copyChannel(v);
//            copyOutChannel.setBackgroundResource(R.drawable.bg_channel_tag_normal);
//            copyOutChannel.setLayoutParams(new LayoutParams(channelWidth, channelHeight));
//            copyOutChannel.setX(v.getX());
//            copyOutChannel.setY(v.getY() + view1.getMeasuredHeight());
//            ChannelGridView.this.addView(copyOutChannel);
//            ViewPropertyAnimator animate = copyOutChannel.animate();
//            if ((channelPoints.size() - 1) % channelColumn == 0) {
//                animate.x(channelPoints.get(0).x)
//                        .y(channelPoints.get(channelPoints.size() - 1).y + view1.getMeasuredHeight() + myChannel.getMeasuredHeight() + view2.getMeasuredHeight())
//                        .setDuration(DURATION_TIME);
//            } else {
//                animate.x(channelPoints.get(channelPoints.size() - 1).x)
//                        .y(channelPoints.get(channelPoints.size() - 1).y + view1.getMeasuredHeight() + view2.getMeasuredHeight() + myChannel.getMeasuredHeight())
//                        .setDuration(DURATION_TIME);
//
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                animate.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        if ((float) animation.getAnimatedValue() > 0) {
//                            v.setVisibility(INVISIBLE);
//                        }
//                    }
//                });
//            }
//            animate.setListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//                        v.setVisibility(INVISIBLE);
//                    }
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    newAddChannel.setVisibility(VISIBLE);
//                    ChannelGridView.this.removeView(copyOutChannel);
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
        }
    }
}
