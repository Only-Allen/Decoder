package com.chx.decoder.decoder.result;

import com.google.gson.annotations.Expose;

public class Bounds {
    @Expose
    private Point topLeft;
    @Expose
    private Point topRight;
    @Expose
    private Point bottomLeft;
    @Expose
    private Point bottomRight;

    public Bounds() {

    }

    public Bounds(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public Point getTopRight() {
        return topRight;
    }

    public void setTopRight(Point topRight) {
        this.topRight = topRight;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(Point bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public Point getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    public Point getMarkPoint() {
        Point point = getMinPoint(topLeft, topRight);
        point = getMinPoint(point, bottomLeft);
        point = getMinPoint(point, bottomRight);
        return point;
    }

    public Point getMinPoint(Point p1, Point p2) {
        return (p1.getX() + p1.getY()) > (p2.getX() + p2.getY()) ? p2 : p1;
    }
}
