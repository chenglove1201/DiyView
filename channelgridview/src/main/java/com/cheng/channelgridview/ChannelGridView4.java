package com.cheng.channelgridview;

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
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChannelGridView4 extends ScrollView {
    private Context mContext;
    private AttributeSet mAttrs;
    private int mDefStyleAttr;
    private Map<String, String[]> channelContents = new LinkedHashMap<>();
    private int channelFixedToPosition = -1;
    private ChannelLayout channelGrid;

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

    /**
     * 真个频道内边距
     */
    private int channelPadding;

    /**
     * 每个频道的间隔
     */
    private int channelSpacing;

    public ChannelGridView4(Context context) {
        this(context, null);
    }

    public ChannelGridView4(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChannelGridView4(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        this.mAttrs = attrs;
        this.mDefStyleAttr = defStyleAttr;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChannelGridView4);
        channelHeight = (int) typedArray.getDimension(R.styleable.ChannelGridView4_channelHeight, 120);
        channelColumn = typedArray.getInteger(R.styleable.ChannelGridView4_channelColumn, 4);
        channelPadding = (int) typedArray.getDimension(R.styleable.ChannelGridView4_channelPadding, 0);
        channelSpacing = (int) typedArray.getDimension(R.styleable.ChannelGridView4_channelSpacing, 0);
        if (channelColumn <= 0) {
            channelColumn = 1;
        }
        if (channelHeight <= 0) {
            channelHeight = 120;
        }
        if (channelPadding <= 0) {
            channelPadding = 0;
        }
        if (channelSpacing <= 0) {
            channelSpacing = 0;
        }
        typedArray.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !isChannelLongClick && super.onInterceptTouchEvent(ev);
    }

    private boolean isChannelLongClick;

    /**
     * 设置前toPosition+1个channel不能拖动
     *
     * @param toPosition 前toPosition个channel不能拖动，toPosition是下标
     */
    public void setFixedChannel(int toPosition) {
        this.channelFixedToPosition = toPosition;
    }

    /**
     * 设置推荐频道
     *
     * @param channels
     */
    public void setRecommendChannels(Map<String, String[]> channels) {
        if (channels != null) {
            this.channelContents = channels;
        }
    }

    /**
     * 获取我的频道
     *
     * @return
     */
    public String[] getMyChannel() {
        String[] channels = null;
        return channels;
    }

    private OnChannelItemClickListener onChannelItemClickListener;

    /**
     * 填充数据
     */
    public void inflateData() {
        channelGrid = new ChannelLayout(mContext, mAttrs, mDefStyleAttr);
        addView(channelGrid);
    }

    public interface OnChannelItemClickListener {
        void channelItemClick(int itemId, String channel);
    }

    public void setOnChannelItemClickListener(OnChannelItemClickListener onChannelItemClickListener) {
        this.onChannelItemClickListener = onChannelItemClickListener;
    }

    private boolean isLayout = true;

    public AnimatorSet animatorSet = new AnimatorSet();

    private class GridViewAttr {
        static final int TITLE = 0x01;
        static final int CHANNEL = 0x02;

        /**
         * view类型
         */
        private int type;

        /**
         * view坐标
         */
        private PointF coordinate;

        /**
         * view所在的channelGroups位置
         */
        private int groupIndex;

        /**
         * 频道归属，用于删除频道时该频道的归属位置（推荐、国内、国外）,默认都为1
         */
        private int belong = 1;
    }

    private class ChannelLayout extends GridLayout implements OnTouchListener, OnLongClickListener, OnClickListener {
        private static final int DURATION_TIME = 200;
        /**
         * 固定频道的颜色
         */
        private final String FIXED_CHANNEL = "#CCCCCC";

        /**
         * 频道普通点击
         */
        private final int NORMAL = 0X00;

        /**
         * 点击删除频道
         */
        private final int DELETE = 0x01;

        private int channelClickType = NORMAL;

        public ChannelLayout(Context context) {
            this(context, null);
        }

        public ChannelLayout(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ChannelLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        /**
         * 是否需要重新测量
         */
        private boolean isMeasure = true;

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (isMeasure) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                if (!isAnimateChangeHeight) {
                    int height = 0;
                    int everyChannelHeight = 0;
                    for (int i = 0; i < getChildCount(); i++) {
                        View childAt = getChildAt(i);
                        if (((GridViewAttr) childAt.getTag()).type == GridViewAttr.TITLE) {
                            childAt.measure(MeasureSpec.makeMeasureSpec(width - (channelSpacing + channelPadding) * 2, MeasureSpec.EXACTLY), heightMeasureSpec);
                            height += childAt.getMeasuredHeight();
                        } else if (((GridViewAttr) childAt.getTag()).type == GridViewAttr.CHANNEL) {
                            channelWidth = (width - channelSpacing * 2 * channelColumn - channelPadding * 2) / channelColumn;
                            childAt.measure(MeasureSpec.makeMeasureSpec(channelWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(channelHeight, MeasureSpec.EXACTLY));
                            everyChannelHeight = childAt.getMeasuredHeight();
                        }
                    }
                    height += allChannelColumns * everyChannelHeight;
                    setMeasuredDimension(width, height);
                } else {
                    setMeasuredDimension(width, animateHeight);
                }
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (isLayout) {
                super.onLayout(changed, left, top, right, bottom);
                for (int i = 0; i < getChildCount(); i++) {
                    View childAt = getChildAt(i);
                    GridViewAttr tag = (GridViewAttr) childAt.getTag();
                    tag.coordinate = new PointF(childAt.getX(), childAt.getY());
                }
                isLayout = false;
            }
        }

        private void init() {
            setColumnCount(channelColumn);
            setAlignmentMode(ALIGN_BOUNDS);
            setPadding(channelPadding, channelPadding, channelPadding, channelPadding);
            addView();
        }

        private List<View> channelTitleGroups = new ArrayList<>();
        private List<ArrayList<View>> channelGroups = new ArrayList<>();

        private int startRow;

        /**
         * 每组channel的行数
         */
        private int[] groupChannelColumns;

        /**
         * 所有频道组成的行数
         */
        private int allChannelColumns;

        private TextView tipEdit, tipFinish;

        private void addView() {
            if (channelContents != null) {
                groupChannelColumns = new int[channelContents.size()];
                int j = 0;
                for (String aKeySet : channelContents.keySet()) {
                    String[] channelContent = channelContents.get(aKeySet);
                    groupChannelColumns[j] = channelContent.length % channelColumn == 0 ? channelContent.length / channelColumn : channelContent.length / channelColumn + 1;
                    allChannelColumns += groupChannelColumns[j];
                    if (j == 0) {
                        startRow = 0;
                    } else {
                        startRow += groupChannelColumns[j - 1] + 1;
                    }
                    GridLayout.Spec rowSpec = GridLayout.spec(startRow);
                    GridLayout.Spec columnSpec = GridLayout.spec(0, channelColumn);
                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
                    View view = LayoutInflater.from(mContext).inflate(R.layout.cgl_my_channel, null);
                    if (j == 0) {
                        tipEdit = view.findViewById(R.id.tv_tip_edit);
                        tipEdit.setVisibility(VISIBLE);
                        tipEdit.setOnClickListener(this);
                        tipFinish = view.findViewById(R.id.tv_tip_finish);
                        tipFinish.setVisibility(INVISIBLE);
                        tipFinish.setOnClickListener(this);
                    }
                    GridViewAttr channelTitleAttr = new GridViewAttr();
                    channelTitleAttr.type = GridViewAttr.TITLE;
                    view.setTag(channelTitleAttr);
                    TextView tvTitle = view.findViewById(R.id.tv_title);
                    tvTitle.setText(aKeySet);
                    addView(view, layoutParams);
                    channelTitleGroups.add(view);
                    ArrayList<View> channelGroup = new ArrayList();
                    for (int i = 0; i < channelContent.length; i++) {
                        TextView textView = new TextView(mContext);
                        GridViewAttr channelAttr = new GridViewAttr();
                        channelAttr.type = GridViewAttr.CHANNEL;
                        channelAttr.groupIndex = j;
                        if (j != 0) {
                            channelAttr.belong = j;
                        }
                        textView.setTag(channelAttr);
                        textView.setText(channelContent[i]);
                        textView.setGravity(Gravity.CENTER);
                        textView.setBackgroundResource(R.drawable.bg_channel_tag_normal);
                        if (j == 0 && i <= channelFixedToPosition) {
                            textView.setTextColor(Color.parseColor(FIXED_CHANNEL));
                        }
                        textView.setOnClickListener(this);
                        textView.setOnTouchListener(this);
                        textView.setOnLongClickListener(this);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.setMargins(channelSpacing, channelSpacing, channelSpacing, channelSpacing);
                        addView(textView, params);
                        channelGroup.add(textView);
                    }
                    channelGroups.add(channelGroup);
                    j++;
                }
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
            if (v == tipFinish) {
                editChannelTip(false);
            } else {
                GridViewAttr tag = (GridViewAttr) v.getTag();
                ArrayList<View> channels = channelGroups.get(tag.groupIndex);
                if (tag.groupIndex == 0) {
                    if (channelClickType == DELETE) {//减少我的频道
                        forwardSort(v, channels);
                        deleteMyChannel(v);
                    } else if (channelClickType == NORMAL) {
                        onChannelItemClickListener.channelItemClick(channels.indexOf(v), ((TextView) v).getText().toString());
                    }
                } else {//增加我的频道
                    forwardSort(v, channels);
                    addMyChannel(v);
                }
            }
        }

        /**
         * 后面的频道向前排序
         *
         * @param v
         * @param channels
         */
        private void forwardSort(View v, ArrayList<View> channels) {
            int size = channels.size();
            int indexOfValue = channels.indexOf(v);
            if (indexOfValue != size - 1) {
                for (int i = size - 1; i > indexOfValue; i--) {
                    View lastView = channels.get(i - 1);
                    GridViewAttr lastViewTag = (GridViewAttr) lastView.getTag();
                    View currentView = channels.get(i);
                    GridViewAttr currentViewTag = (GridViewAttr) currentView.getTag();
                    currentViewTag.coordinate = lastViewTag.coordinate;
                    currentView.animate().x(currentViewTag.coordinate.x).y(currentViewTag.coordinate.y).setDuration(DURATION_TIME);
                }
            }
        }


        /**
         * 增加我的频道
         *
         * @param v
         */
        private void addMyChannel(View v) {
            GridViewAttr tag = (GridViewAttr) v.getTag();
            ArrayList<View> channels = channelGroups.get(tag.groupIndex);
            ArrayList<View> myChannels = channelGroups.get(0);
            View finalMyChannel = myChannels.get(myChannels.size() - 1);
            View firstMyChannel = myChannels.get(0);
            GridViewAttr finalMyChannelTag = (GridViewAttr) finalMyChannel.getTag();
            GridViewAttr firstMyChannelTag = (GridViewAttr) firstMyChannel.getTag();
            myChannels.add(myChannels.size(), v);
            channels.remove(v);
            animateChangeGridViewHeight();
            if (myChannels.size() % channelColumn == 1) {
                tag.coordinate = new PointF(firstMyChannelTag.coordinate.x, finalMyChannelTag.coordinate.y + channelHeight);
                v.animate().x(tag.coordinate.x).y(tag.coordinate.y).setDuration(DURATION_TIME);
                //我的频道多一行，下面的view往下移
                viewMove(1, channelHeight);
            } else {
                tag.coordinate = new PointF(finalMyChannelTag.coordinate.x + channelWidth, finalMyChannelTag.coordinate.y);
                v.animate().x(tag.coordinate.x).y(tag.coordinate.y).setDuration(DURATION_TIME);
            }
            //该频道少一行，下面的view往上移
            if (channels.size() % channelColumn == 0) {
                viewMove(tag.groupIndex + 1, -channelHeight);
            }
            tag.groupIndex = 0;
        }

        /**
         * 删除我的频道
         *
         * @param v
         */
        private void deleteMyChannel(View v) {
            GridViewAttr tag = (GridViewAttr) v.getTag();
            ArrayList<View> beLongChannels = channelGroups.get(tag.belong);
            GridViewAttr arriveTag = (GridViewAttr) beLongChannels.get(0).getTag();
            tag.coordinate = arriveTag.coordinate;
            v.animate().x(tag.coordinate.x).y(tag.coordinate.y).setDuration(DURATION_TIME);
            beLongChannels.add(0, v);
            channelGroups.get(0).remove(v);
            animateChangeGridViewHeight();
            PointF newPointF;
            GridViewAttr finalChannelViewTag = (GridViewAttr) beLongChannels.get(beLongChannels.size() - 1).getTag();
            //这个地方要注意顺序
            if (channelGroups.get(0).size() % channelColumn == 0) {
                //我的频道中少了一行，底下的所有view全都上移
                viewMove(1, -channelHeight);
            }
            if (beLongChannels.size() % channelColumn == 1) {
                //回收来频道中多了一行，底下的所有view全都下移
                viewMove(tag.belong + 1, channelHeight);
                newPointF = new PointF(arriveTag.coordinate.x, finalChannelViewTag.coordinate.y + channelHeight);
            } else {
                newPointF = new PointF(finalChannelViewTag.coordinate.x + channelWidth, finalChannelViewTag.coordinate.y);
            }
            for (int i = 1; i < beLongChannels.size(); i++) {
                View currentView = beLongChannels.get(i);
                GridViewAttr currentViewTag = (GridViewAttr) currentView.getTag();
                if (i < beLongChannels.size() - 1) {
                    View nextView = beLongChannels.get(i + 1);
                    GridViewAttr nextViewTag = (GridViewAttr) nextView.getTag();
                    currentViewTag.coordinate = nextViewTag.coordinate;
                } else {
                    currentViewTag.coordinate = newPointF;
                }
                currentView.animate().x(currentViewTag.coordinate.x).y(currentViewTag.coordinate.y).setDuration(DURATION_TIME);
            }
            tag.groupIndex = tag.belong;
        }

        private int animateHeight;
        private boolean isAnimateChangeHeight;

        /**
         * 重新计算行数变化后的gridview高度并用动画改变
         */
        private void animateChangeGridViewHeight() {
            int newAllChannelColumns = 0;
            for (int i = 0; i < channelGroups.size(); i++) {
                ArrayList<View> channels = channelGroups.get(i);
                newAllChannelColumns += channels.size() % channelColumn == 0 ? channels.size() / channelColumn : channels.size() / channelColumn + 1;
            }
            final int changeHeight = (newAllChannelColumns - allChannelColumns) * channelHeight;
            if (changeHeight != 0) {
                allChannelColumns = newAllChannelColumns;
                ValueAnimator valueAnimator = ValueAnimator.ofInt(getMeasuredHeight(), getMeasuredHeight() + changeHeight);
                valueAnimator.setDuration(DURATION_TIME);
                valueAnimator.start();
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        animateHeight = (int) animation.getAnimatedValue();
                        isAnimateChangeHeight = true;
                        requestLayout();
                    }
                });
                valueAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAnimateChangeHeight = false;
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

        /**
         * 受到行数所影响的view进行上移或下移操作
         */
        private void viewMove(int position, int offSetY) {
            for (int i = position; i < channelTitleGroups.size(); i++) {
                View view = channelTitleGroups.get(i);
                GridViewAttr tag = (GridViewAttr) view.getTag();
                tag.coordinate = new PointF(tag.coordinate.x, tag.coordinate.y + offSetY);
                view.animate().x(tag.coordinate.x).y(tag.coordinate.y).setDuration(DURATION_TIME);
            }
            for (int i = position; i < channelGroups.size(); i++) {
                ArrayList<View> otherChannels = channelGroups.get(i);
                for (int j = 0; j < otherChannels.size(); j++) {
                    View view = otherChannels.get(j);
                    GridViewAttr tag = (GridViewAttr) view.getTag();
                    tag.coordinate = new PointF(tag.coordinate.x, tag.coordinate.y + offSetY);
                    view.animate().x(tag.coordinate.x).y(tag.coordinate.y).setDuration(DURATION_TIME);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            v.bringToFront();
            GridViewAttr tag = (GridViewAttr) v.getTag();
            if (tag.groupIndex == 0) {
                editChannelTip(true);
            }
            return true;
        }

        float downX, downY;
        float moveX, moveY;

        /**
         * 频道拖动
         */
        private void channelDrag(View v, MotionEvent event) {
            moveX = event.getRawX();
            moveY = event.getRawY();
            v.setX(v.getX() + (moveX - downX));
            v.setY(v.getY() + (moveY - downY));
            downX = moveX;
            downY = moveY;
            ArrayList<View> myChannels = channelGroups.get(0);
            GridViewAttr vTag = (GridViewAttr) v.getTag();
            int vIndex = myChannels.indexOf(v);
            for (int i = 0; i < myChannels.size(); i++) {
                if (i > channelFixedToPosition && i != vIndex) {
                    View iChannel = myChannels.get(i);
                    GridViewAttr iChannelTag = (GridViewAttr) iChannel.getTag();
                    int x1 = (int) iChannelTag.coordinate.x;
                    int y1 = (int) iChannelTag.coordinate.y;
                    int sqrt = (int) Math.sqrt((v.getX() - x1) * (v.getX() - x1) + (v.getY() - y1) * (v.getY() - y1));
                    if (sqrt <= 70 && !animatorSet.isRunning()) {
                        animatorSet = new AnimatorSet();
                        PointF tempPoint = iChannelTag.coordinate;
                        ObjectAnimator[] objectAnimators = new ObjectAnimator[Math.abs(i - vIndex) * 2];
                        if (i < vIndex) {
                            for (int j = i; j < vIndex; j++) {
                                TextView view = (TextView) myChannels.get(j);
                                GridViewAttr viewTag = (GridViewAttr) view.getTag();
                                GridViewAttr nextGridViewAttr = ((GridViewAttr) myChannels.get(j + 1).getTag());
                                viewTag.coordinate = nextGridViewAttr.coordinate;
                                objectAnimators[2 * (j - i)] = ObjectAnimator.ofFloat(view, "X", viewTag.coordinate.x);
                                objectAnimators[2 * (j - i) + 1] = ObjectAnimator.ofFloat(view, "Y", viewTag.coordinate.y);
                            }
                        } else if (i > vIndex) {
                            for (int j = i; j > vIndex; j--) {
                                TextView view = (TextView) myChannels.get(j);
                                GridViewAttr viewTag = (GridViewAttr) view.getTag();
                                GridViewAttr preGridViewAttr = ((GridViewAttr) myChannels.get(j - 1).getTag());
                                viewTag.coordinate = preGridViewAttr.coordinate;
                                objectAnimators[2 * (j - vIndex - 1)] = ObjectAnimator.ofFloat(view, "X", viewTag.coordinate.x);
                                objectAnimators[2 * (j - vIndex - 1) + 1] = ObjectAnimator.ofFloat(view, "Y", viewTag.coordinate.y);
                            }
                        }
                        animatorSet.playTogether(objectAnimators);
                        animatorSet.setDuration(DURATION_TIME);
                        isMeasure = false;
                        animatorSet.start();
                        vTag.coordinate = tempPoint;
                        myChannels.remove(v);
                        myChannels.add(i, v);
                        break;
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
            isMeasure = true;
            isChannelLongClick = false;
            GridViewAttr vTag = (GridViewAttr) v.getTag();
            v.animate().x(vTag.coordinate.x).y(vTag.coordinate.y).setDuration(DURATION_TIME);
        }

        private void editChannelTip(boolean state) {
            ArrayList<View> views = channelGroups.get(0);
            if (state) {
                tipFinish.setVisibility(VISIBLE);
                tipEdit.setVisibility(INVISIBLE);
                channelClickType = DELETE;
                isChannelLongClick = true;
                for (int i = 0; i < views.size(); i++) {
                    if (i > channelFixedToPosition) {
                        views.get(i).setBackgroundResource(R.drawable.bg_channel_tag_selected);
                    }
                }
            } else {
                tipFinish.setVisibility(INVISIBLE);
                tipEdit.setVisibility(VISIBLE);
                channelClickType = NORMAL;
                isChannelLongClick = false;
                for (int i = 0; i < views.size(); i++) {
                    if (i > channelFixedToPosition) {
                        views.get(i).setBackgroundResource(R.drawable.bg_channel_tag_normal);
                    }
                }
            }
        }
    }

}
