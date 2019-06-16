package com.chx.decoder.comparator;

import com.chx.decoder.decoder.result.DecoderResult;

public class ResultComparatorByRow extends IResultComparator {
    @Override
    public int compare(DecoderResult lhs, DecoderResult rhs) {
        int value = diffOfXWithoutError(lhs, rhs);
        if (value == 0) {
            value = diffOfY(lhs, rhs);
        }
        return value;
    }
}
