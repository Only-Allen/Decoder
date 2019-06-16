package com.chx.decoder.event;

import android.graphics.Rect;

public class ROISelectedEvent {
    //增加ROI
    private Rect rect;

    public ROISelectedEvent() {
    }

    public ROISelectedEvent(Rect rect) {
        this.rect = rect;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}
