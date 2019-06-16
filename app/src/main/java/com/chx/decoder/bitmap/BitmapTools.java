package com.chx.decoder.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;

public class BitmapTools {
    public static Bitmap cameraDataToBitmap(byte[] data, int width, int height) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg( new Rect(0, 0,yuvimage.getWidth(), yuvimage.getHeight())
                , 100, baos);
        byte[] rawImage =baos.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        SoftReference<Bitmap> softRef = new SoftReference<Bitmap>(
                BitmapFactory.decodeByteArray(rawImage, 0,rawImage.length,options));//方便回收
        Bitmap bitmap = (Bitmap) softRef.get();
        return bitmap;
    }
}
