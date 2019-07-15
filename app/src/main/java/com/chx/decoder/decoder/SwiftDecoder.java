package com.chx.decoder.decoder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.chx.decoder.decoder.result.DecoderResult;
import com.honeywell.barcode.DecodeManager;
import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.Symbology;
import com.honeywell.misc.HSMLog;

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

    private DecodeManager decodeManager;

    private SwiftDecoder(Context context) {
        decodeManager = DecodeManager.getInstance(context);
        initDecodeManager();
    }

    private void initDecodeManager() {
        decodeManager.enableDecoding(true);
        enableSymbology(Symbology.UPCA);
        enableSymbology(Symbology.CODE128);
        enableSymbology(Symbology.CODE39);
        enableSymbology(Symbology.QR);
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

    public static int activateIfNeed(Context context) {
        return DecodeManager.getInstance(context).activate("");
    }

    //return 0 means error occur
    public int decode(Bitmap bitmap) {

        return 0;
    }

    public List<DecoderResult> decode(byte[] bytes, int width, int height) {
        HSMDecodeResult[] results = decodeManager.decode(bytes, width, height);
        return DecoderResult.toDecoderResults(results);
    }

    public boolean enableSymbology(int symbology) {
        try {
            int[] symbs = new int[]{symbology};
            return this.enableSymbology(symbs);
        } catch (Exception var3) {
            HSMLog.e(var3);
            return false;
        }
    }

    public boolean enableSymbology(int[] symbologies) {
        try {
            HSMLog.trace();
            HashMap<String, String> params = new HashMap();
            int[] var3 = symbologies;
            int i = symbologies.length;

            int res;
            for (int var5 = 0; var5 < i; ++var5) {
                res = var3[var5];
                params.put("SymbologyId", String.valueOf(res));
            }

            boolean result = true;

            for (i = 0; i < symbologies.length; ++i) {
                int val;
                if (symbologies[i] >= 1 && symbologies[i] <= 30) {
                    res = decodeManager.SetProperty(437321729, symbologies[i]);
                    result &= res == 1;
                } else {
                    switch (symbologies[i]) {
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
            HSMLog.e(var7);
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
