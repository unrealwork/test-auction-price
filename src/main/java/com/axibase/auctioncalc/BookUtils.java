package com.axibase.auctioncalc;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class BookUtils {
    public static long[] totalBids(List<Order> bids, int startTotal) {
        long[] result = new long[bids.size()];
        int start = 0;
        int end = bids.size();
        int step = 1;
        for (int i = start; i != end; i += step) {
            int count = bids.get(i).getCount();
            result[i] = i == start ? startTotal + count : result[i - step] + count;
        }
        return result;
    }
}
