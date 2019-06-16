package com.chx.decoder.activity;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.chx.decoder.R;
import com.chx.decoder.bitmap.BitmapTools;
import com.chx.decoder.camera.CameraTools;
import com.chx.decoder.constants.Constants;
import com.chx.decoder.decoder.result.Point;

public class CameraActivity extends DecodeActivity {

    private SurfaceView mSurfaceView;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_camera;
    }

    @Override
    public void initView() {
        super.initView();
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                CameraTools.getInstance().open();
                CameraTools.getInstance().setup(surfaceHolder);
                CameraTools.getInstance().setCallback(new CameraTools.OnPreviewCallback() {
                    @Override
                    public void onPreview(byte[] data) {
                        onDecodeImage(data);
                    }
                });
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                CameraTools.getInstance().start();
//                CameraTools.getInstance().startFocus();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                CameraTools.getInstance().close();
            }
        });
    }

    @Override
    public void onDecodeClick() {
        CameraTools.getInstance().doAutoFocus();
    }

    @Override
    public void onResultClick() {
        CameraTools.getInstance().start();
    }

    //begin decode
    public void onDecodeImage(byte[] data) {
        Bitmap bitmap = BitmapTools.cameraDataToBitmap(
                data, Constants.PREVIEW_WIDTH, Constants.PREVIEW_HEIGHT);
        decodeBitmap(bitmap);
    }

    @Override
    public void beforeShowResults() {
        super.beforeShowResults();
        CameraTools.getInstance().stop();
    }

    @Override
    public Point getViewPointByBitmapPoint(Point point) {
        Point ret = new Point();
        ret.setX(point.getX() * mSurfaceView.getWidth() / Constants.PREVIEW_WIDTH);
        ret.setY(point.getY() * mSurfaceView.getHeight() / Constants.PREVIEW_HEIGHT);
        return ret;
    }
}
