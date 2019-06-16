package com.chx.decoder.comparator;

import android.util.Log;

import com.chx.decoder.constants.Constants;
import com.chx.decoder.decoder.result.DecoderResult;

import java.util.Comparator;

public abstract class IResultComparator implements Comparator<DecoderResult> {
    protected void warnTooNear() {
        Log.w("ResultComparator", "two code is too near!!");
    }

    //返回x坐标的差值，以行输出时用这个
    protected int diffOfX(DecoderResult resultL, DecoderResult resultR) {
        return resultL.getCenter().getX() - resultR.getCenter().getX();
    }

    //返回x坐标的差值，以列输出时用这个
    protected int diffOfXWithoutError(DecoderResult resultL, DecoderResult resultR) {
        int value = resultL.getCenter().getX() - resultR.getCenter().getX();
        if (Math.abs(value) < Constants.ERROR_OF_PIXEL_ROW) {
            value = 0;
        }
        return value;
    }
    //返回y坐标的差值，以列输出时用这个
    protected int diffOfY(DecoderResult resultL, DecoderResult resultR) {
        return resultL.getCenter().getY() - resultR.getCenter().getY();
    }

    //返回y坐标的差值，以行输出时用这个
    protected int diffOfYWithoutError(DecoderResult resultL, DecoderResult resultR) {
        int value = resultL.getCenter().getY() - resultR.getCenter().getY();
        if (Math.abs(value) < Constants.ERROR_OF_PIXEL_LINE) {
            value = 0;
        }
        return value;
    }
}
