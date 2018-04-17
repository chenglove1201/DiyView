package com.cheng.channelgridview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChannelGridView extends ScrollView {
    private ChannelGrid channelGrid;

    public ChannelGridView(Context context) {
        this(context, null);
    }

    public ChannelGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChannelGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        channelGrid = new ChannelGrid(context, attrs, defStyleAttr);
        addView(channelGrid);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !isChannelLongClick && super.onInterceptTouchEvent(ev);
    }

    private boolean isChannelLongClick;

    /**
     * 设置我的频道
     *
     * @return
     */
    public void setMyChannel(String[] channel) {
        channelGrid.setMyChannel(channel);
    }

    /**
     * 设置推荐频道
     *
     * @return
     */
    public void setRecommendChannel(String[] channel) {
        channelGrid.setRecommendChannel(channel);
    }

    /**
     * 获取我的频道
     *
     * @return
     */
    public String[] getMyChannel() {
        return channelGrid.getMyChannel();
    }

    /**
     * 获取推荐频道
     *
     * @return
     */
    public String[] getRecommendChannel() {
        return channelGrid.getRecommendChannel();
    }

    private OnChannelItemClickListener onChannelItemClickListener;

    public interface OnChannelItemClickListener {
        void channelItemClick(int itemId, String channel);
    }

    public void setOnChannelItemClickListener(OnChannelItemClickListener onChannelItemClickListener) {
        this.onChannelItemClickListener = onChannelItemClickListener;
    }

    private class ChannelGrid extends LinearLayout {
        private Channel myChannel, recommendChannel;
        private View view1, view2;
        private TextView view1Tip;

        public ChannelGrid(Context context) {
            this(context, null);
        }

        public ChannelGrid(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ChannelGrid(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
            addView(myChannel);
            view2 = LayoutInflater.from(context).inflate(R.layout.cgl_my_channel, null);
            TextView view2Title = view2.findViewById(R.id.tv_title);
            final TextView view2Tip = view2.findViewById(R.id.tv_tip);
            view2Title.setText("推荐频道");
            view2Tip.setText("点击添加频道");
            addView(view2);
            recommendChannel = new Channel(context, attrs, defStyleAttr);
            addView(recommendChannel);
            setOrientation(VERTICAL);
        }

        /**
         * 设置我的频道
         *
         * @return
         */
        private void setMyChannel(String[] channel) {
            myChannel.content = channel;
            myChannel.addView();
            myChannel.setEnableDrag(true);
        }

        /**
         * 设置推荐频道
         *
         * @return
         */
        private void setRecommendChannel(String[] channel) {
            recommendChannel.content = channel;
            recommendChannel.addView();
            recommendChannel.setEnableDrag(false);
        }

        /**
         * 获取我的频道
         *
         * @return
         */
        private String[] getMyChannel() {
            String[] channels = new String[myChannel.channelViews.size()];
            for (int i = 0; i < myChannel.channelViews.size(); i++) {
                channels[i] = ((TextView) myChannel.channelViews.get(i)).getText().toString();
            }
            return channels;
        }

        /**
         * 获取推荐频道
         *
         * @return
         */
        private String[] getRecommendChannel() {
            String[] channels = new String[recommendChannel.channelViews.size()];
            for (int i = 0; i < recommendChannel.channelViews.size(); i++) {
                channels[i] = ((TextView) recommendChannel.channelViews.get(i)).getText().toString();
            }
            return channels;
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

        private class Channel extends ViewGroup implements OnTouchListener, OnClickListener, OnLongClickListener {
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

//        private String[] content = {"要闻", "视频", "新时代", "娱乐", "体育", "军事", "NBA", "国际", "科技"};

            private String[] content;

            /**
             * 频道坐标
             */
            private SparseArray<PointF> channelPoints = new SparseArray<>();

            /**
             * 频道集合
             */
            private SparseArray<View> channelViews = new SparseArray<>();
            ;

            /**
             * 动画持续时间
             */
            private final int DURATION_TIME = 200;

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
            }

            private void addView() {
                if (content != null) {
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
            }

            /**
             * 设置是否能拖拽
             */
            private void setEnableDrag(boolean isEnableDrag) {
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
                    int rowCount = 0, spaceCount = 0;
                    if (childCount != 0) {
                        rowCount = childCount % channelColumn == 0 ? childCount / channelColumn : childCount / channelColumn + 1;
                        spaceCount = childCount % channelColumn == 0 ? childCount / channelColumn - 1 : childCount / channelColumn;
                    }
                    height = rowCount * channelHeight + verticalSpacing * spaceCount + channelTopPadding + channelBottomPadding;
                }
                if (!isDynamicAddHeight) {//不是动态的增加高度
                    setMeasuredDimension(width, height);
                } else {//动态的增加高度
                    setMeasuredDimension(width, dynamicHeight);
                }
            }

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
                    if (onChannelItemClickListener != null) {
                        onChannelItemClickListener.channelItemClick(channelViews.indexOfValue(v), ((TextView) v).getText().toString());
                    }
                } else if (channelClickType == ADD) {
                    //推荐频道中，要做相应的移除一个view后的过渡动画
                    if (!animatorSet.isRunning()) {
                        //在myChannel需要操作的事情
                        clickAddChannel(v);
                        //在recommendChannel需要操作的事情
                        animateClickChannel(v);
                    }
                } else if (channelClickType == DELETE) {
                    if (!animatorSet.isRunning()) {
                        clickRecycleChannel(v);
                        animateClickChannel(v);
                    }
                }
            }

            /**
             * 点击频道（myChannel和recommendChannel）时在本频道发生得动作
             *
             * @param v
             */
            private void animateClickChannel(final View v) {
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
                if (channelViews.size() % channelColumn == 0) {
                    isDynamicAddHeight = true;
                    ValueAnimator dynamicHeightAnimator;
                    if (channelViews.size() == 0) {
                        dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() - channelHeight);
                    } else {
                        dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() - channelHeight - verticalSpacing);
                    }
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

            @Override
            public boolean onLongClick(View v) {
                if (!animatorSet.isRunning()) {
                    view1Tip.setText("完成");
                    channelClickType = DELETE;
                    isChannelLongClick = true;
                    copyChannel = new TextView(mContext);
                    copyChannel.setLayoutParams(new LayoutParams(channelWidth, channelHeight));
                    copyChannel.setText(((TextView) v).getText());
                    copyChannel.setGravity(Gravity.CENTER);
                    copyChannel.setBackgroundResource(R.drawable.bg_channel_tag_focused);
//                copyChannel.measure(MeasureSpec.makeMeasureSpec(channelWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(channelHeight, MeasureSpec.EXACTLY));
//                copyChannel.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    copyChannel.setX(v.getX());
                    copyChannel.setY(v.getY() - getMeasuredHeight() - view2.getMeasuredHeight() - recommendChannel.getMeasuredHeight());
                    ChannelGrid.this.addView(copyChannel);
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
                        ChannelGrid.this.removeView(copyChannel);
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
            private void stopDrag() {
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

            private void addChannel(final View v) {
                final TextView newAddChannel = copyChannel(v);
                newAddChannel.setOnTouchListener(this);
                newAddChannel.setOnLongClickListener(this);
                newAddChannel.setOnClickListener(this);
                newAddChannel.setVisibility(INVISIBLE);
                if (channelClickType == DELETE) {
                    newAddChannel.setBackgroundResource(R.drawable.bg_channel_tag_selected);
                } else {
                    newAddChannel.setBackgroundResource(R.drawable.bg_channel_tag_normal);
                }
                channelViews.put(channelViews.size(), newAddChannel);
                if (channelViews.size() % channelColumn == 1) {
                    isDynamicAddHeight = true;
                }
                int childAt = channelViews.size() - 1;
                int flag = childAt % channelColumn;
                View childView = channelViews.get(childAt);
                childView.layout(channelWidth * flag + horizontalSpacing * flag + channelLeftPadding,
                        (channelHeight + verticalSpacing) * ((childAt) / channelColumn) + channelTopPadding,
                        channelWidth * (flag + 1) + horizontalSpacing * flag + channelLeftPadding,
                        (channelHeight + verticalSpacing) * ((childAt) / channelColumn) + channelHeight + channelTopPadding);
                addView(newAddChannel);
                if (channelPoints.size() % channelColumn == 0) {
                    if (channelPoints.size() != 0) {
                        channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(0).x,
                                channelPoints.get(channelPoints.size() - 1).y + verticalSpacing + channelHeight));
                    } else {
                        channelPoints.put(0, new PointF(channelLeftPadding, channelTopPadding));
                    }
                } else {
                    channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(channelPoints.size() - 1).x + channelWidth + horizontalSpacing,
                            channelPoints.get(channelPoints.size() - 1).y));
                }
                if (isDynamicAddHeight) {
                    ValueAnimator dynamicHeightAnimator;
                    if ((channelViews.size() - 1) == 0) {
                        dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() + channelHeight);
                    } else {
                        dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() + channelHeight + verticalSpacing);
                    }
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
                ChannelGrid.this.addView(copyOutChannel);
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
//                            v.setVisibility(INVISIBLE);
                                recommendChannel.removeView(v);
                            }
                        }
                    });
                }
                animate.setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//                        v.setVisibility(INVISIBLE);
                            recommendChannel.removeView(v);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        newAddChannel.setVisibility(VISIBLE);
                        ChannelGrid.this.removeView(copyOutChannel);
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
                return textView;
            }

            /**
             * 删除我的频道
             *
             * @param v
             */
            private void recycleChannel(final View v) {
                final TextView newAddChannel = copyChannel(v);
                newAddChannel.setOnClickListener(this);
                newAddChannel.layout(channelLeftPadding, channelTopPadding, channelLeftPadding + channelWidth, channelTopPadding + channelHeight);
                newAddChannel.setVisibility(INVISIBLE);
                newAddChannel.setBackgroundResource(R.drawable.bg_channel_tag_normal);
                int currentSize = channelViews.size();
                for (int i = currentSize; i > 0; i--) {
                    channelViews.put(i, channelViews.get(i - 1));
                }
                channelViews.put(0, newAddChannel);
                if (channelViews.size() % channelColumn == 1) {
                    isDynamicAddHeight = true;
                }
                addView(newAddChannel);
                if (channelPoints.size() % channelColumn == 0) {
                    if (channelPoints.size() != 0) {
                        channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(0).x,
                                channelPoints.get(channelPoints.size() - 1).y + verticalSpacing + channelHeight));
                    } else {
                        channelPoints.put(0, new PointF(channelLeftPadding, channelTopPadding));
                    }
                } else {
                    channelPoints.put(channelPoints.size(), new PointF(channelPoints.get(channelPoints.size() - 1).x + channelWidth + horizontalSpacing,
                            channelPoints.get(channelPoints.size() - 1).y));
                }
                for (int i = 0; i < channelViews.size(); i++) {
                    channelViews.get(i).animate().x(channelPoints.get(i).x).y(channelPoints.get(i).y).setDuration(DURATION_TIME);
                }
                if (isDynamicAddHeight) {
                    ValueAnimator dynamicHeightAnimator;
                    if ((channelViews.size() - 1) == 0) {
                        dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() + channelHeight);
                    } else {
                        dynamicHeightAnimator = ObjectAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() + channelHeight + verticalSpacing);
                    }
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
                copyOutChannel.setBackgroundResource(R.drawable.bg_channel_tag_selected);
                copyOutChannel.setLayoutParams(new LayoutParams(channelWidth, channelHeight));
                copyOutChannel.setX(v.getX());
                copyOutChannel.setY(v.getY() + view1.getMeasuredHeight());
                ChannelGrid.this.addView(copyOutChannel);
                ViewPropertyAnimator animate = copyOutChannel.animate();
                if ((myChannel.channelViews.size() - 1) % channelColumn == 0) {
                    if (myChannel.channelViews.size() != 1) {
                        animate.x(channelPoints.get(0).x)
                                .y(channelPoints.get(0).y + view1.getMeasuredHeight() + view2.getMeasuredHeight() + myChannel.getMeasuredHeight() - verticalSpacing - channelHeight)
                                .setDuration(DURATION_TIME);
                    } else {
                        animate.x(channelPoints.get(0).x)
                                .y(channelPoints.get(0).y + view1.getMeasuredHeight() + view2.getMeasuredHeight() + myChannel.getMeasuredHeight() - channelHeight)
                                .setDuration(DURATION_TIME);
                    }
                } else {
                    animate.x(channelPoints.get(0).x)
                            .y(channelPoints.get(0).y + view1.getMeasuredHeight() + view2.getMeasuredHeight() + myChannel.getMeasuredHeight())
                            .setDuration(DURATION_TIME);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    animate.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if ((float) animation.getAnimatedValue() > 0) {
//                            v.setVisibility(INVISIBLE);
                                myChannel.removeView(v);
                            }
                        }
                    });
                }
                animate.setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//                        v.setVisibility(INVISIBLE);
                            myChannel.removeView(v);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        newAddChannel.setVisibility(VISIBLE);
                        ChannelGrid.this.removeView(copyOutChannel);
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
