package com.cheng.diyview.wheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ViewDragHelper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.cheng.diyview.R;


/**
 * Created by ICAN on 2017/5/25.
 */

public class WheelView extends LinearLayout {
    private TextView textView4;
    private int lastX;
    private int lastY;
    private Scroller scroller;
    private ViewDragHelper viewDragHelper;
    private LinearLayout hour, minute;
    private int unitHeight;
    private Context context;
    private String[] hourTime, minuteTime;
    private int currintHour, currentMinute;
    private int topHour, downHour, topMinute, downMinute;
    private final float MAX_SCALE = 1.5f, MIN_SCALE = 1.0f;
    private float textSize = 20f;
    private int marginSize = 20, paddingSize = 10;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        setWillNotDraw(false);

        textView4 = new TextView(context);
        scroller = new Scroller(context);
        hour = new LinearLayout(context);
        minute = new LinearLayout(context);

        hour.setOrientation(LinearLayout.VERTICAL);
        minute.setOrientation(LinearLayout.VERTICAL);

        hourTime = new String[]{intToStringTime(16), intToStringTime(17), intToStringTime(18),
                intToStringTime(19), intToStringTime(20)};
        currintHour = Integer.parseInt(hourTime[2]);
        topHour = Integer.parseInt(hourTime[0]);
        downHour = Integer.parseInt(hourTime[4]);

        minuteTime = new String[]{intToStringTime(30), intToStringTime(31), intToStringTime(32),
                intToStringTime(33), intToStringTime(34)};
        currentMinute = Integer.parseInt(minuteTime[2]);
        topMinute = Integer.parseInt(minuteTime[0]);
        downMinute = Integer.parseInt(minuteTime[4]);

        for (String aHourTime : hourTime) {
            TextView textView = new TextView(context);
            textView.setText(aHourTime);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(textSize);
            hour.addView(textView);
        }

        for (String aHourTime : minuteTime) {
            TextView textView = new TextView(context);
            textView.setText(aHourTime);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(textSize);
            minute.addView(textView);
        }

        textView4.setText(":");
        textView4.setGravity(Gravity.CENTER);
        textView4.setTextSize(textSize);
        hour.getChildAt(2).setScaleX(MAX_SCALE);
        hour.getChildAt(2).setScaleY(MAX_SCALE);
        minute.getChildAt(2).setScaleX(MAX_SCALE);
        minute.getChildAt(2).setScaleY(MAX_SCALE);

        addView(hour);
        addView(textView4);
        addView(minute);
        setGravity(Gravity.CENTER);

        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(marginSize, 0, marginSize, 0);
        textView4.setLayoutParams(layoutParams);

        TextView t = ((TextView) hour.getChildAt(2));
        TextPaint paint = t.getPaint();
        Rect rect = new Rect();
        paint.getTextBounds(t.getText().toString(), 0, t.getText().toString().length(), rect);

        LayoutParams timeLayout = new LayoutParams((int) (rect.right * MAX_SCALE),
                LayoutParams.WRAP_CONTENT);
        hour.setLayoutParams(timeLayout);
        minute.setLayoutParams(timeLayout);

        viewDrag();

        hour.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getY() >= unitHeight * 3 && event.getY() <= unitHeight * 4)
                    clickPosition = false;
                else if (event.getY() >= unitHeight && event.getY() <= unitHeight * 2)
                    clickPosition = true;
                else
                    clickPosition = null;
                return false;
            }
        });
    }

    /**
     * 点击位置，true表示上半部分，false表示下半部分
     */
    private Boolean clickPosition = null;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST ? hour.getWidth() * 2 + textView4.getWidth() + marginSize * 2 + paddingSize : MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST ? hour.getHeight() / hour.getChildCount() * 3 : MeasureSpec.getSize(heightMeasureSpec);
        unitHeight = heightSize / 3;
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        setBackgroundResource(R.drawable.background_wheel);
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.background_wheel_val);
        drawable.setBounds(0, unitHeight, hour.getWidth() * 2 + textView4.getWidth() + marginSize * 2 + paddingSize, unitHeight * 2);
        drawable.draw(canvas);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
//        if (scroller.computeScrollOffset()) {
//            ((View) getParent()).scrollTo(scroller.getCurrX(), scroller.getCurrY());
//            invalidate();
//        }
        if (viewDragHelper.continueSettling(true))
            invalidate();
        else {
            if (flag)
                changeHour();
        }
    }

    public void viewDrag() {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            private int x, y = -unitHeight;
            /**
             * 滚动方向，true是向下滚动，false向上
             */
            private boolean rollDirection;

            /**
             * 是否执行完一次完整的向上或向下滚动
             */
            private boolean rollDownFinish = true, rollUpFinish = true;

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                x = child.getLeft();
                return child == hour || child == minute;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return child.getLeft();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                float enlarge = MIN_SCALE, narrow = MAX_SCALE;
                float minBaseValue = MAX_SCALE - MIN_SCALE, maxBaseVlaue = MAX_SCALE + MIN_SCALE;

                if (rollDirection) {
                    if (!rollUpFinish) {
                        enlarge = -(float) top / (unitHeight * 2) + minBaseValue;
                        narrow = maxBaseVlaue - enlarge;

                        ((LinearLayout) changedView).getChildAt(3).setScaleX(enlarge);
                        ((LinearLayout) changedView).getChildAt(3).setScaleY(enlarge);
                        ((LinearLayout) changedView).getChildAt(2).setScaleX(narrow);
                        ((LinearLayout) changedView).getChildAt(2).setScaleY(narrow);
                        if (top >= -unitHeight)
                            rollUpFinish = true;
                    } else {
                        rollDownFinish = false;
                        if (top < 0) {
                            enlarge = (float) top / unitHeight * minBaseValue + MAX_SCALE;
                            narrow = maxBaseVlaue - enlarge;
                        }
                        if (flag) {
                            enlarge = (float) top / unitHeight * minBaseValue + MAX_SCALE;
                            narrow = maxBaseVlaue - enlarge;
                        }
                        ((LinearLayout) changedView).getChildAt(1).setScaleX(enlarge);
                        ((LinearLayout) changedView).getChildAt(1).setScaleY(enlarge);
                        ((LinearLayout) changedView).getChildAt(2).setScaleX(narrow);
                        ((LinearLayout) changedView).getChildAt(2).setScaleY(narrow);
                    }
                }
                if (!rollDirection) {
                    if (!rollDownFinish) {
                        enlarge = (float) top / unitHeight * minBaseValue + MAX_SCALE;
                        narrow = maxBaseVlaue - enlarge;

                        ((LinearLayout) changedView).getChildAt(1).setScaleX(enlarge);
                        ((LinearLayout) changedView).getChildAt(1).setScaleY(enlarge);
                        ((LinearLayout) changedView).getChildAt(2).setScaleX(narrow);
                        ((LinearLayout) changedView).getChildAt(2).setScaleY(narrow);
                        if (top <= -unitHeight)
                            rollDownFinish = true;
                    } else {
                        rollUpFinish = false;
                        if (top > -unitHeight * 2) {
                            enlarge = -(float) top / (unitHeight * 2) + minBaseValue;
                            narrow = maxBaseVlaue - enlarge;
                        }
                        if (flag) {
                            enlarge = -(float) top / unitHeight / 2 + minBaseValue;
                            narrow = maxBaseVlaue - enlarge;
                        }
                        ((LinearLayout) changedView).getChildAt(3).setScaleX(enlarge);
                        ((LinearLayout) changedView).getChildAt(3).setScaleY(enlarge);
                        ((LinearLayout) changedView).getChildAt(2).setScaleX(narrow);
                        ((LinearLayout) changedView).getChildAt(2).setScaleY(narrow);
                    }
                }
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                int calculateHeight = Math.abs(top) / unitHeight;
                rollDirection = dy > 0;

                if (child == hour) {
                    if (top >= 0) {
                        hourDown();
                    }

                    if (top <= -unitHeight * 2) {
                        hourUp();
                    }
                } else {
                    if (top >= 0) {
                        minuteDown();
                    }

                    if (top <= -unitHeight * 2) {
                        minuteUp();
                    }
                }

                if (top > -unitHeight * 2 && top <= -(unitHeight + unitHeight / 2)) {
                    y = -unitHeight * calculateHeight * 2;
                    if (child == hour)
                        ROLL = HOUR_UP_UROLL;
                    else
                        ROLL = MINUTE_UP_ROLL;
                } else if (top > -(unitHeight + unitHeight / 2) && top <= -unitHeight)
                    y = -unitHeight;
                else if (top > -unitHeight && top <= -unitHeight / 2)
                    y = -unitHeight * (calculateHeight + 1);
                else if (top > -unitHeight / 2 && top <= 0) {
                    y = 0;
                    if (child == hour)
                        ROLL = HOUR_DOWN_ROLL;
                    else
                        ROLL = MINUTE_DOWN_ROLL;
                } else
                    y = -unitHeight * calculateHeight;

                return top;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (clickPosition == null && isClick)
                    return;

                if (scrollDownY <= 10) {
                    isClick = true;
                    scrollDownY = 0;
                    startDownY = 0;
                } else
                    isClick = false;
                if (isClick & clickPosition != null) {
                    if (clickPosition) {
                        rollDirection = true;
                        y = 0;
                        if (releasedChild == hour)
                            ROLL = HOUR_DOWN_ROLL;
                        else
                            ROLL = MINUTE_DOWN_ROLL;
                    } else {
                        rollDirection = false;
                        y = -unitHeight * 2;
                        if (releasedChild == hour)
                            ROLL = HOUR_UP_UROLL;
                        else
                            ROLL = MINUTE_UP_ROLL;
                    }
                }
                if (y == 0 || y == -unitHeight * 2)
                    flag = true;

                viewDragHelper.settleCapturedViewAt(x, y);
                invalidate();
            }
        });
    }

    /**
     * 是否是点击而不是滑动
     */
    private boolean isClick;
    private float startDownY, scrollDownY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            startDownY = event.getY();
        if (event.getAction() == MotionEvent.ACTION_MOVE)
            scrollDownY = Math.abs(event.getY() - startDownY);
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    private String intToStringTime(int time) {
        if (time >= 10)
            return String.valueOf(time);
        else
            return "0" + String.valueOf(time);
    }

    private boolean flag;

    private int ROLL = 0;
    private final int HOUR_UP_UROLL = 0x10;
    private final int HOUR_DOWN_ROLL = 0x11;
    private final int MINUTE_UP_ROLL = 0x12;
    private final int MINUTE_DOWN_ROLL = 0x13;

    private void changeHour() {
        switch (ROLL) {
            case HOUR_UP_UROLL:
                hourUp();
                break;
            case HOUR_DOWN_ROLL:
                hourDown();
                break;
            case MINUTE_UP_ROLL:
                minuteUp();
                break;
            case MINUTE_DOWN_ROLL:
                minuteDown();
                break;
            default:
                break;
        }
        flag = false;
    }

    /**
     * hour向上滚动
     */
    private void hourDown() {
        TextView textView = new TextView(context);
        if (topHour == 1) {
            topHour = 24;
            textView.setText("00");
        } else {
            topHour = topHour - 1;
            textView.setText(intToStringTime(topHour));
        }
        downHour = topHour + 4;
        if (downHour <= 28 && downHour >= 24) {
            downHour = downHour - 24;
        }
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(textSize);
        hour.removeViewAt(4);
        hour.addView(textView, 0);
        hour.getChildAt(2).setScaleX(MAX_SCALE);
        for (int i = 0; i < hour.getChildCount(); i++) {
            if (i == 2) {
                hour.getChildAt(i).setScaleX(MAX_SCALE);
                hour.getChildAt(i).setScaleY(MAX_SCALE);
            } else {
                hour.getChildAt(i).setScaleX(MIN_SCALE);
                hour.getChildAt(i).setScaleY(MIN_SCALE);
            }
        }
    }

    /**
     * hour向下滚动
     */
    private void hourUp() {
        TextView textView = new TextView(context);
        if (downHour == 23) {
            downHour = 0;
            textView.setText("00");
        } else {
            downHour = downHour + 1;
            textView.setText(intToStringTime(downHour));
        }
        topHour = downHour - 4;
        if (topHour >= -4 && topHour <= 0) {
            topHour = topHour + 24;
        }
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(textSize);
        hour.removeViewAt(0);
        hour.addView(textView, 4);
        for (int i = 0; i < hour.getChildCount(); i++) {
            if (i == 2) {
                hour.getChildAt(i).setScaleX(MAX_SCALE);
                hour.getChildAt(i).setScaleY(MAX_SCALE);
            } else {
                hour.getChildAt(i).setScaleX(MIN_SCALE);
                hour.getChildAt(i).setScaleY(MIN_SCALE);
            }
        }
    }

    /**
     * minute向上滚动
     */
    private void minuteDown() {
        TextView textView = new TextView(context);
        if (topMinute == 1) {
            topMinute = 60;
            textView.setText("00");
        } else {
            topMinute = topMinute - 1;
            textView.setText(intToStringTime(topMinute));
        }
        downMinute = topMinute + 4;
        if (downMinute <= 64 && downMinute >= 60) {
            downMinute = downMinute - 60;
        }
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(textSize);
        minute.removeViewAt(4);
        minute.addView(textView, 0);
        for (int i = 0; i < hour.getChildCount(); i++) {
            if (i == 2) {
                minute.getChildAt(i).setScaleX(MAX_SCALE);
                minute.getChildAt(i).setScaleY(MAX_SCALE);
            } else {
                minute.getChildAt(i).setScaleX(MIN_SCALE);
                minute.getChildAt(i).setScaleY(MIN_SCALE);
            }
        }
    }

    /**
     * minute向下滚动
     */
    private void minuteUp() {
        TextView textView = new TextView(context);
        if (downMinute == 59) {
            downMinute = 0;
            textView.setText("00");
        } else {
            downMinute = downMinute + 1;
            textView.setText(intToStringTime(downMinute));
        }
        topMinute = downMinute - 4;
        if (topMinute >= -4 && topMinute <= 0) {
            topMinute = topMinute + 60;
        }
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(textSize);
        minute.removeViewAt(0);
        minute.addView(textView, 4);
        for (int i = 0; i < hour.getChildCount(); i++) {
            if (i == 2) {
                minute.getChildAt(i).setScaleX(MAX_SCALE);
                minute.getChildAt(i).setScaleY(MAX_SCALE);
            } else {
                minute.getChildAt(i).setScaleX(MIN_SCALE);
                minute.getChildAt(i).setScaleY(MIN_SCALE);
            }
        }
    }
}
