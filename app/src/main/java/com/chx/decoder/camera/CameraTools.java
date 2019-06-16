package com.chx.decoder.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.chx.decoder.constants.Constants;

import java.util.List;

public class CameraTools {

    private static CameraTools sCameraTools;
    public static final String TAG = "CameraTools";
    private Camera mCamera;
    private boolean isPreviewing;

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {//success表示对焦成功
//                Log.i(TAG, "onAutoFocus succeed...");
                decodeImage();
            } else {
                Log.w(TAG, "onAutoFocus failed...");
            }
        }
    };

    private OnPreviewCallback mCallback;

    private static final int INTERVAL_FOCUS = 3000;
    private static final int HANDLER_FOCUS = 0;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private CameraTools() {
        mHandlerThread = new HandlerThread("camera_focus");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLER_FOCUS:
                        doAutoFocus();
                        this.sendEmptyMessageDelayed(HANDLER_FOCUS, INTERVAL_FOCUS);
                        break;
                }
            }
        };
    }

    public static CameraTools getInstance() {
        if (sCameraTools == null) {
            synchronized (CameraTools.class) {
                if (sCameraTools == null) {
                    sCameraTools = new CameraTools();
                }
            }
        }
        return sCameraTools;
    }

    public void setCallback(OnPreviewCallback callback) {
        mCallback = callback;
    }

    public void open() {
        if (Constants.DEBUG) {
            Log.d(TAG, "open camera, number=" + Camera.getNumberOfCameras());
        }
        if (Camera.getNumberOfCameras() == 0) {
            return;
        }
        mCamera = Camera.open(0);
    }

    public void setup(SurfaceHolder holder) {
        if (Constants.DEBUG) {
            Log.d(TAG, "setup camera");
        }
        if (mCamera == null) {
            Log.w(TAG, "camera is null when start");
            return;
        }
        try {
            Camera.Parameters parameter = mCamera.getParameters();
//            printSupportedSize(parameter);
//            printSupportedFormats(parameter);
            parameter.setPreviewSize(Constants.PREVIEW_WIDTH, Constants.PREVIEW_HEIGHT);
            parameter.setPictureSize(Constants.PREVIEW_WIDTH, Constants.PREVIEW_HEIGHT);
//            parameter.setPreviewFormat(ImageFormat.RGB_565);
            mCamera.setParameters(parameter);
//            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            Log.e(TAG, "init camera failed!", e);
        }
    }

    public void printSupportedSize(Camera.Parameters parameter) {
        List<Camera.Size> previewSizes = parameter.getSupportedPreviewSizes();
        Log.i(TAG, "---------supported sizes of preview---------");
        for (Camera.Size size : previewSizes) {
            Log.i(TAG, size.width + " x " + size.height);
        }
    }

    public void printSupportedFormats(Camera.Parameters parameter) {
        List<Integer> formats = parameter.getSupportedPictureFormats();
        Log.i(TAG, "---------supported formats of preview---------");
        for (int f : formats) {
            Log.i(TAG, "format: " + f);
        }
    }

    public void start() {
        if (Constants.DEBUG) {
            Log.d(TAG, "start preview");
        }
        if (mCamera == null) {
            Log.w(TAG, "camera is null when start");
            return;
        }
        if (!isPreviewing) {
            mCamera.startPreview();
//            startFocus();
            isPreviewing = true;
        }
    }

    public void stop() {
        if (Constants.DEBUG) {
            Log.d(TAG, "stop preview");
        }
        if (mCamera == null) {
            Log.w(TAG, "camera is null when stop");
            return;
        }
        if (isPreviewing) {
            mCamera.stopPreview();
            isPreviewing = false;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void close() {
        if (Constants.DEBUG) {
            Log.d(TAG, "close camera");
        }
        if (mCamera == null) {
            Log.w(TAG, "camera is null when close");
            return;
        }
        stop();
        mHandlerThread.quitSafely();
        mCamera.release();
        mCamera = null;
    }

    public void decodeImage() {
        if (Constants.DEBUG) {
            Log.d(TAG, "decode image");
        }
        if (mCamera == null) {
            Log.w(TAG, "camera is null when decodeImage");
            return;
        }
        if (mCallback == null) {
            Log.w(TAG, "callback is null when decodeImage");
            return;
        }
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                mCallback.onPreview(bytes);
                mCamera.setPreviewCallback(null);
            }
        });
    }

    public void doAutoFocus() {
        if (Constants.DEBUG) {
            Log.d(TAG, "do auto focus");
        }
        if (mCamera == null) {
            Log.w(TAG, "camera is null when doAutoFocus");
            return;
        }
        mCamera.autoFocus(myAutoFocusCallback);
    }

    public void startFocus() {
        if (Constants.DEBUG) {
            Log.d(TAG, "start focus");
        }
        mHandler.sendEmptyMessage(HANDLER_FOCUS);
    }

    public void stopFocus() {
        if (Constants.DEBUG) {
            Log.d(TAG, "stop focus");
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface OnPreviewCallback {
        void onPreview(byte[] data);
    }

}
