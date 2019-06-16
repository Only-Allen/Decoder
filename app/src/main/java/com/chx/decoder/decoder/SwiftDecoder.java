package com.chx.decoder.decoder;

import android.graphics.Bitmap;
import android.util.Log;

import com.chx.decoder.constants.Constants;
import com.chx.decoder.decoder.result.DecoderResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SwiftDecoder {

    static {
        System.loadLibrary("native-lib");
    }

    private static SwiftDecoder swiftDecoder;

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
}
