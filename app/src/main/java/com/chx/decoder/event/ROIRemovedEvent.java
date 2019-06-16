package com.chx.decoder.event;

import android.graphics.Rect;

public class ROIRemovedEvent {
    //ROI删除
    private Rect rect;

    public ROIRemovedEvent() {
    }

    public ROIRemovedEvent(Rect rect) {
        this.rect = rect;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}
