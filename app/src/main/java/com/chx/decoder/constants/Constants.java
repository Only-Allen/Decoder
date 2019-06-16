package com.chx.decoder.constants;

public interface Constants {
    int PREVIEW_WIDTH = 1920;
    int PREVIEW_HEIGHT = 1080;
    boolean DEBUG = true;
    //y坐标差小于这个认为是同一行
    int ERROR_OF_PIXEL_LINE = 20;
    //x坐标差小于这个认为是同一列
    int ERROR_OF_PIXEL_ROW = 20;
}
