package com.chx.decoder.decoder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.chx.decoder.decoder.result.DecoderResult;
import com.honeywell.barcode.DecodeManager;
import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.barcode.Symbology;
import com.honeywell.barcode.WindowMode;
import com.honeywell.license.ActivationManager;
import com.honeywell.misc.HSMLog;
import com.honeywell.plugins.decode.DecodeResultListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class SwiftDecoder {

    static {
        System.loadLibrary("native-lib");
    }

    private static SwiftDecoder swiftDecoder;
    private static final String FILE_NAME = "IdentityClient.bin";

    private static final String TAG = "SwiftDecoder";

//    private HSMDecoder mHSMDecoder;
    private DecodeManager decodeManager;

    private SwiftDecoder(Context context) {
//        mHSMDecoder = HSMDecoder.getInstance(context);
        decodeManager = DecodeManager.getInstance(context);
        initDecodeManager();
//        mHSMDecoder.addResultListener(new DecodeResultListener() {
//            @Override
//            public void onHSMDecodeResult(HSMDecodeResult[] hsmDecodeResults) {
//                Log.d(TAG, "");
//            }
//        });
    }

    private void initDecodeManager() {
//        mHSMDecoder.releaseCameraConnection();
//        mHSMDecoder.enableSymbology(Symbology.UPCA);
//        mHSMDecoder.enableSymbology(Symbology.CODE128);
//        mHSMDecoder.enableSymbology(Symbology.CODE39);
//        mHSMDecoder.enableSymbology(Symbology.QR);
        int[] symbols = new int[] {Symbology.UPCA, Symbology.CODE128, Symbology.CODE39,
                Symbology.QR, Symbology.DATAMATRIX, Symbology.DATAMATRIX_RECTANGLE};
        enableSymbology(symbols);
    }

    public static SwiftDecoder getInstance(Context context) {
        if (swiftDecoder == null) {
            synchronized (SwiftDecoder.class) {
                if (swiftDecoder == null) {
                    swiftDecoder = new SwiftDecoder(context);
                }
            }
        }
        return swiftDecoder;
    }

    public static void release() {
        DecodeManager.destroyInstance();
        swiftDecoder = null;
    }

    public static void activateIfNeed(Context context) {
        if (!ActivationManager.isActivated(context)) {
            ActivationManager.activate(context, "trial-test1-tjian-02012019");
        }
    }

    //return 0 means error occur
    public int decode(Bitmap bitmap) {

        return 0;
    }

    public List<DecoderResult> decode(byte[] bytes, int width, int height) {
//        HSMDecodeResult[] results = mHSMDecoder.decodeImage(bytes, width, height);
        HSMDecodeResult[] results = decodeManager.decode(bytes, width, height);
        return DecoderResult.toDecoderResults(results);
    }

    public boolean enableSymbology(int[] symbologies) {
        try {
            HSMLog.trace();
            HashMap<String, String> params = new HashMap();
            int[] var3 = symbologies;
            int i = symbologies.length;

            int res;
            for(int var5 = 0; var5 < i; ++var5) {
                res = var3[var5];
                params.put("SymbologyId", String.valueOf(res));
            }

            boolean result = true;

            for(i = 0; i < symbologies.length; ++i) {
                if (symbologies[i] >= 1 && symbologies[i] <= 30) {
                    res = decodeManager.SetProperty(437321729, symbologies[i]);
                    result &= res == 1;
                } else {
                    byte val;
                    switch(symbologies[i]) {
                        case 436367361:
                        case 436375553:
                            val = 3;
                            break;
                        case 436371457:
                            val = 127;
                            break;
                        case 436379649:
                            val = 15;
                            break;
                        default:
                            val = 1;
                    }

                    res = decodeManager.SetProperty(symbologies[i], val);
                    result &= res == 1;
                }
            }

            return result;
        } catch (Exception var7) {
            Log.e(TAG, "enable symbology failed", var7);
            return false;
        }
    }

    public List<DecoderResult> getResults() {
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
