package com.chx.decoder.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.chx.decoder.R;
import com.chx.decoder.decoder.result.Point;

public class ImageActivity extends DecodeActivity {

    private static final String TAG = "ImageActivity";
    private final int PICK_IMAGE = 457544821;
    private ImageView mImageView;
    private int mWidth, mHeight;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_image;
    }

    @Override
    public void initView() {
        super.initView();
        mImageView = (ImageView) findViewById(R.id.image_view);
    }

    @Override
    public void onDecodeClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onResultClick() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri selectedImageUri = data.getData();
                try
                {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    mImageView.setImageBitmap(bitmap);
                    mWidth = bitmap.getWidth();
                    mHeight = bitmap.getHeight();
                    decodeBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "generate bitmap failed!", e);
                }
            }
        }
    }

    @Override
    public Point getViewPointByBitmapPoint(Point point) {
        if ((mWidth * mImageView.getHeight()) > (mHeight * mImageView.getWidth())) {
            //图片过宽
            float scale = ((float) mImageView.getWidth()) / mWidth;
            int startY = (int) ((mImageView.getHeight() - mHeight * scale) / 2);
            Point ret = new Point();
            ret.setX((int) (point.getX() * scale));
            ret.setY((int) (point.getY() * scale + startY));
            return ret;
        } else {
            float scale = ((float) mImageView.getHeight()) / mHeight;
            int startX = (int) ((mImageView.getWidth() - mWidth * scale) / 2);
            Point ret = new Point();
            ret.setX((int) (point.getX() * scale + startX));
            ret.setY((int) (point.getY() * scale));
            return ret;
        }
    }
}
