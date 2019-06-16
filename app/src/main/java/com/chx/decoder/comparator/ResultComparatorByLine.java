package com.chx.decoder.comparator;

import com.chx.decoder.decoder.result.DecoderResult;

public class ResultComparatorByLine extends IResultComparator {
    @Override
    public int compare(DecoderResult lhs, DecoderResult rhs) {
        int value = diffOfYWithoutError(lhs, rhs);
        if (value == 0) {
            value = diffOfX(lhs, rhs);
        }
        return value;
    }
}
