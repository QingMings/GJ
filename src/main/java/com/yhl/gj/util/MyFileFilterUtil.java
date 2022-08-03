package com.yhl.gj.util;

import org.apache.commons.io.filefilter.IOFileFilter;

public class MyFileFilterUtil {

    public static IOFileFilter fileNameAge24hFilter(final boolean acceptOlder) {
        // 最新24h
        long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        return new FileNameAgeFileFilter(cutoff, acceptOlder);
    }

}
