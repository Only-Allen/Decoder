package com.chx.decoder.region;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.chx.decoder.event.ROIFinishedEvent;
import com.chx.decoder.event.ROIRemovedEvent;
import com.chx.decoder.event.ROISelectedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class DrawView extends View {

    private PointF downPoint, currentPoint;
    private Paint mPaint;
    private Rect mRect;
    private List<Rect> mRects;
    private boolean isMove;

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        EventBus.getDefault().register(this);

        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);


        mRects = new ArrayList<>();
//        mRect = new Rect();
//        mRect.contains();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (downPoint != null) {
            fillCurrentRect();
            canvas.drawRect(mRect, mPaint);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onMotionDown(event);
                isMove = false;
                break;
            case MotionEvent.ACTION_MOVE:
                onMotionMove(event);
                isMove = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!isMove) {
                    EventBus.getDefault().post(new ROIFinishedEvent());
                    mRects.clear();
                } else {
                    onMotionUp(event);
                }
                break;
        }
        return true;
    }

    public void onMotionDown(MotionEvent event) {
        downPoint = new PointF(event.getX(), event.getY());
        currentPoint = new PointF();
        mRect = new Rect();
    }

    public void onMotionMove(MotionEvent event) {
        fillCurrentPoint(event);
        invalidate();
    }

    public void onMotionUp(MotionEvent event) {
        fillCurrentPoint(event);
        fillCurrentRect();
        downPoint = null;
        currentPoint = null;
        invalidate();
        EventBus.getDefault().post(new ROISelectedEvent(mRect));
        mRects.add(mRect);
    }

    public void fillCurrentRect() {
        mRect.set((int) Math.min(downPoint.x, currentPoint.x),
                (int) Math.min(downPoint.y, currentPoint.y),
                (int) Math.max(downPoint.x, currentPoint.x),
                (int) Math.max(downPoint.y, currentPoint.y));
    }

    public void fillCurrentPoint(MotionEvent event) {
        boolean intersect = false;
        for (Rect rect : mRects) {
            if (Rect.intersects(rect, new Rect((int) Math.min(downPoint.x, event.getX()),
                    (int) Math.min(downPoint.y, event.getY()),
                    (int) Math.max(downPoint.x, event.getX()),
                    (int) Math.max(downPoint.y, event.getY())))) {//和当前某个矩形有重叠
                intersect = true;
                break;
            }
        }
        if (!intersect) {
            currentPoint.set(event.getX(), event.getY());
            return;
        }

        intersect = false;
        for (Rect rect : mRects) {
            if (Rect.intersects(rect, new Rect((int) Math.min(downPoint.x, currentPoint.x),
                    (int) Math.min(downPoint.y, event.getY()),
                    (int) Math.max(downPoint.x, currentPoint.x),
                    (int) Math.max(downPoint.y, event.getY())))) {//和当前某个矩形在Y方向有重叠
                intersect = true;
                break;
            }
        }
        if (!intersect) {
            currentPoint.y = event.getY();
            return;
        }

        intersect = false;
        for (Rect rect : mRects) {
            if (Rect.intersects(rect, new Rect((int) Math.min(downPoint.x, event.getX()),
                    (int) Math.min(downPoint.y, currentPoint.y),
                    (int) Math.max(downPoint.x, event.getX()),
                    (int) Math.max(downPoint.y, currentPoint.y)))) {//和当前某个矩形在X方向有重叠
                intersect = true;
                break;
            }
        }
        if (!intersect) {
            currentPoint.x = event.getX();
        }
    }

    @Subscribe
    public void onROIRemoved(ROIRemovedEvent event) {
        mRects.remove(event.getRect());
    }
}
