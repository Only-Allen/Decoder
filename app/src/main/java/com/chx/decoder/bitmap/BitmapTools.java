package com.chx.decoder.bitmap;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BitmapTools {

    public static final String TAG = "BitmapTools";

    public static Bitmap cameraDataToBitmap(byte[] data, int width, int height) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, yuvimage.getWidth(), yuvimage.getHeight())
                , 100, baos);
        byte[] rawImage = baos.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        SoftReference<Bitmap> softRef = new SoftReference<Bitmap>(
                BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options));//方便回收
        Bitmap bitmap = (Bitmap) softRef.get();
        return bitmap;
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int length = width * height;
        byte[] bytes = new byte[length * 4];
        Buffer dst = ByteBuffer.wrap(bytes);
        bitmap.copyPixelsToBuffer(dst);
        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++) {
            ret[i] = (byte) ((bytes[i * 4] + bytes[i * 4 + 1] + bytes[i * 4 + 2] ) / 3);
        }
        return ret;
    }

    public static native byte[] getBytesToDecode(Bitmap bitmap);

    public static native byte[] bmpFileToBytes(String path);

    public static byte[] getBmpFileBytes(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "file not exists!");
            return null;
        }
        FileInputStream fis = null;
        byte[] buffer = null;
        try {
            fis = new FileInputStream(file);
            int countAll = fis.available();
            fis.skip(1078);
            buffer = new byte[countAll - 1078];
            int count = fis.read(buffer, 0, buffer.length);
            Log.d(TAG, "read count: " + count);
        } catch (Exception e) {
            Log.e(TAG, "read file buffer failed!", e);
            try {
                fis.close();
            } catch (Exception e1) {
                Log.e(TAG, "close file input stream failed!", e);
            }
        }
        return buffer;
    }

    public static Bitmap getBitmapFormUri(ContentResolver resolver, Uri uri, int width, int height) throws FileNotFoundException, IOException {
        InputStream input = resolver.openInputStream(uri);
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        int originalWidth = bitmapOptions.outWidth;
        int originalHeight = bitmapOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        Log.d(TAG, originalWidth + "..." + originalHeight);
        int be = Math.min(originalWidth / width, originalHeight / height);

        if (be <= 0) {//如果缩放比比1小，那么保持原图不缩放
            be = 1;
        }
//        be = 1;
        Log.d(TAG, "scale : " + be);
        //比例压缩
        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inSampleSize = be;//设置缩放比例
        input = resolver.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        Log.d(TAG, "bitmap returning width:" + bitmap.getWidth() + ", height:" + bitmap.getHeight());
        return bitmap;//再进行质量压缩
    }

}
