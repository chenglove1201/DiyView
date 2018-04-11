package com.cheng.diyview.channel;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
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
        TextView view1Tip = view1.findViewById(R.id.tv_tip);
        view1Tip.setText("已选频道");
        view1Tip.setText("按住拖拽频道");
        addView(view1);
        myChannel = new Channel(context, attrs, defStyleAttr);
        myChannel.setEnableDrag(true);
        addView(myChannel);
        view2 = LayoutInflater.from(context).inflate(R.layout.cgl_my_channel, null);
        TextView view2Title = view2.findViewById(R.id.tv_title);
        TextView view2Tip = view2.findViewById(R.id.tv_tip);
        view2Title.setText("推荐频道");
        view2Tip.setText("点击添加频道");
        view2Tip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                myChannel.addChannel();
            }
        });
        addView(view2);
        recommendChannel = new Channel(context, attrs, defStyleAttr);
        recommendChannel.setEnableDrag(false);
        addView(recommendChannel);
        setOrientation(VERTICAL);
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

        private String[] content = {"要闻", "视频", "新时代", "娱乐", "体育", "军事", "NBA", "国际", "科技", "财经", "汽车", "电影", "游戏", "独家", "房产"};

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
                for (int i = 0; i < channelViews.size(); i++) {
                    channelViews.get(i).setOnTouchListener(this);
                    channelViews.get(i).setOnLongClickListener(this);
                }
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
            setMeasuredDimension(width, height);
        }

        /**
         * 是否增加频道
         */
        private boolean isAddChannel;

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
            if (isAddChannel) {
                int childAt = channelViews.size() - 1;
                int flag = childAt % channelColumn;
                View childView = channelViews.get(childAt);
                childView.layout(channelWidth * flag + horizontalSpacing * flag + channelLeftPadding,
                        (channelHeight + verticalSpacing) * ((childAt) / channelColumn) + channelTopPadding,
                        channelWidth * (flag + 1) + horizontalSpacing * flag + channelLeftPadding,
                        (channelHeight + verticalSpacing) * ((childAt) / channelColumn) + channelHeight + channelTopPadding);
                channelPoints.put(childAt, new PointF(childView.getX(), childView.getY()));
                isAddChannel = false;
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

        @Override
        public void onClick(View v) {
            Log.i("jfoiwjeijgiwegwe", "channel onclick");
        }

        @Override
        public boolean onLongClick(View v) {
            isChannelLongClick = true;
            copyChannel = new TextView(mContext);
            copyChannel.setLayoutParams(new LinearLayout.LayoutParams(channelWidth, channelHeight));
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
            for (int i = 0; i < channelViews.size(); i++) {
                channelViews.get(i).setBackgroundResource(R.drawable.bg_channel_tag_normal);
            }
        }

        /**
         * 添加频道
         */
        public void addChannel() {
            TextView textView = new TextView(mContext);
            textView.setText("阿基米");
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.bg_channel_tag_normal);
            textView.setOnTouchListener(this);
            textView.setOnLongClickListener(this);
            textView.setOnClickListener(this);
            isAddChannel = true;
            channelViews.put(channelViews.size(), textView);
            addView(textView);
        }
    }
}
