package com.chx.decoder.decoder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.chx.decoder.constants.Constants;
import com.chx.decoder.decoder.result.DecoderResult;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SwiftDecoder {

    static {
        System.loadLibrary("native-lib");
    }

    private static SwiftDecoder swiftDecoder;
    private static final String FILE_NAME = "IdentityClient.bin";

    private static final String TAG = "SwiftDecoder";
    private int mHandle;
    private Gson gson;

    private SwiftDecoder() {
        mHandle = createSD();
        if (mHandle == 0) {
            Log.e(TAG, "create SD failed!!");
        }
        gson = new Gson();
    }

    public static SwiftDecoder getInstance() {
        if (swiftDecoder == null) {
            synchronized (SwiftDecoder.class) {
                if (swiftDecoder == null) {
                    swiftDecoder = new SwiftDecoder();
                }
            }
        }
        return swiftDecoder;
    }

    private native int createSD();

    private native int destroySD(int handle);

    private native int decode(int handle, Bitmap bitmap);

    private native String getResult();

    private static native int activateWithLocalServer(String filename, String path);

    //1-success, 0-failed
    private static native int isActivated(String filename);

    public static int activateIfNeed(Context context) {
        String path = getActivateFilePath(context);
        if (isActivated(path) != 1) {
            int ret = copyAssetsFile2Phone(context);
            if (ret != 0) {
                return ret;
            }
            return activateWithLocalServer(path, path.substring(0, path.lastIndexOf("/")));
        }
        return 0;
    }

    //return 0 means error occur
    public int decode(Bitmap bitmap) {
        if (mHandle == 0) {
            Log.e(TAG, "handle is 0 when decode");
            return 0;
        }
        int result = decode(mHandle, bitmap);
        if (result == 0) {
            Log.e(TAG, "decode failed!!");
        }
        return result;
    }

    //return 0 means error occur
    public int release() {
        if (mHandle == 0) {
            Log.e(TAG, "handle is 0 when release");
        }
        int result = destroySD(mHandle);
        if (result == 0) {
            Log.e(TAG, "destroy SD failed!!");
        }
        swiftDecoder = null;
        return result;
    }

    public List<DecoderResult> getResults() {
        String string = getResult();
        if (string != null && !string.equalsIgnoreCase("")) {
            if (string.endsWith("\n")) {
                string = string.substring(0, string.length() - 1);
            }
            String[] results = string.split("\n");
            if (results.length > 0) {
                List<DecoderResult> decoderResults = new ArrayList<>();
                Log.d(TAG, "number of results : " + results.length);
                for (String result : results) {
                    if (Constants.DEBUG) {
                        Log.d(TAG, "result : " + result);
                    }
                    DecoderResult decoderResult = gson.fromJson(result, DecoderResult.class);
                    decoderResults.add(decoderResult);
                }
                return decoderResults;
            }
        }
        return null;
    }

    public static int copyAssetsFile2Phone(Context context) {
        Log.d(TAG, "prepare to copy bin file");
        try {
            InputStream inputStream = context.getAssets().open(FILE_NAME);
            //getFilesDir() 获得当前APP的安装路径 /data/data/ 目录
            Log.d(TAG, "package name:" + context.getPackageName());
            @SuppressLint("SdCardPath") File file = new File(getActivateFilePath(context));
            if (!file.exists() || file.length() == 0) {
                //如果文件不存在，FileOutputStream会自动创建文件
                FileOutputStream fos = new FileOutputStream(file);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                //刷新缓存区
                fos.flush();
                inputStream.close();
                fos.close();
                Log.d(TAG, "copy bin file succeed");
                return 0;
            } else {
                Log.w(TAG, "bin file is exist!");
                return 0;
            }
        } catch (IOException e) {
            Log.e(TAG, "copy bin file failed!", e);
            return -2;
        }
    }

    public static String getActivateFilePath(Context context) {
        StringBuilder sb = new StringBuilder();
        Log.d(TAG, "the path of context:" + context.getFilesDir().getPath());
        sb.append("/data/data/").append(context.getPackageName()).append("/").append(FILE_NAME);
        return sb.toString();
    }
}
