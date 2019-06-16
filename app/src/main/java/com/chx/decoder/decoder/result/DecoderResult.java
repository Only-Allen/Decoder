package com.chx.decoder.decoder.result;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

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

    public static void main(String[] args) {
        String s = "{\"length\":7,\"center\":{\"x\":20,\"y\":80},\"bounds\":{\"topLeft\":{\"x\":20,\"y\":80},\"topRight\":{\"x\":20,\"y\":80},\"bottomLeft\":{\"x\":20,\"y\":80},\"bottomRight\":{\"x\":20,\"y\":80}},\"result\":\"2222222\"}";
        DecoderResult result = new Gson().fromJson(s, DecoderResult.class);
        System.out.println("success: " + new Gson().toJson(result));
    }
}
