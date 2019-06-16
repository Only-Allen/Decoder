package com.chx.decoder.comparator;

public class ComparatorFactory {
    public enum Type {
        LINE,
        ROW
    }

    public static IResultComparator getComparator(Type type) {
        IResultComparator comparator = null;
        switch (type) {
            case LINE:
                comparator = new ResultComparatorByLine();
                break;
            case ROW:
                comparator = new ResultComparatorByRow();
                break;
        }
        return comparator;
    }
}
