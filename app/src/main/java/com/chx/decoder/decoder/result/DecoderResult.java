package com.chx.decoder.decoder.result;

import android.graphics.Rect;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.honeywell.barcode.BarcodeBounds;
import com.honeywell.barcode.HSMDecodeResult;

import java.util.ArrayList;
import java.util.List;

public class DecoderResult {
    @Expose
    private Point center;
    @Expose
    private Bounds bounds;
    @Expose
    private String result;
    @Expose
    private int length;

    public DecoderResult() {

    }

    public DecoderResult(Point center, Bounds bounds, String result, int length) {
        this.center = center;
        this.bounds = bounds;
        this.result = result;
        this.length = length;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setSiteWithRect(Rect rect) {
        if (center == null) {
            center = new Point();
        }
        center.setX((rect.left + rect.right) / 2);
        center.setY((rect.top + rect.bottom) / 2);

        if (bounds == null) {
            bounds = new Bounds(new Point(), new Point(), new Point(), new Point());
        }
        bounds.getTopLeft().setX(rect.left);
        bounds.getTopLeft().setY(rect.top);
        bounds.getTopRight().setX(rect.right);
        bounds.getTopRight().setY(rect.top);
        bounds.getBottomLeft().setX(rect.left);
        bounds.getBottomLeft().setY(rect.bottom);
        bounds.getBottomRight().setX(rect.right);
        bounds.getBottomRight().setY(rect.bottom);
    }

    public static DecoderResult toDecodeResult(HSMDecodeResult result) {
        DecoderResult ret = new DecoderResult();
        ret.setResult(result.getBarcodeData());
        ret.setLength(result.getBarcodeDataLength());
        ret.setBounds(toBounds(result.getBarcodeBounds()));
        if (ret.getBounds() != null) {
            Point center = new Point();
            center.setX((ret.getBounds().getTopLeft().getX() + ret.getBounds().getTopRight().getX()) / 2);
            center.setY((ret.getBounds().getTopLeft().getY() + ret.getBounds().getBottomLeft().getY()) / 2);
            ret.setCenter(center);
        }
        return ret;
    }

    public static List<DecoderResult> toDecoderResults(HSMDecodeResult[] results) {
        if (results == null) {
            return null;
        }
        List<DecoderResult> resultList = new ArrayList<>();
        for (HSMDecodeResult result : results) {
            DecoderResult d = toDecodeResult(result);
            resultList.add(d);
        }
        return resultList;
    }

    public static Bounds toBounds(BarcodeBounds bounds) {
        Bounds b = new Bounds();
        b.setTopLeft(new Point(bounds.getTopLeft().x, bounds.getTopLeft().y));
        b.setTopLeft(new Point(bounds.getTopRight().x, bounds.getTopRight().y));
        b.setTopLeft(new Point(bounds.getBottomLeft().x, bounds.getBottomLeft().y));
        b.setTopLeft(new Point(bounds.getBottomRight().x, bounds.getBottomRight().y));
        return b;
    }

    public static void main(String[] args) {
        String s = "{\"length\":7,\"center\":{\"x\":20,\"y\":80},\"bounds\":{\"topLeft\":{\"x\":20,\"y\":80},\"topRight\":{\"x\":20,\"y\":80},\"bottomLeft\":{\"x\":20,\"y\":80},\"bottomRight\":{\"x\":20,\"y\":80}},\"result\":\"2222222\"}";
        DecoderResult result = new Gson().fromJson(s, DecoderResult.class);
        System.out.println("success: " + new Gson().toJson(result));
    }
}
