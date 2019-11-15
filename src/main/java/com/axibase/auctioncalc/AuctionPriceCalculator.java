package com.axibase.auctioncalc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuctionPriceCalculator {
    private static int best(CalculationResult r1, CalculationResult r2) {
        if (r1.getMatchedVol() > r2.getMatchedVol()) {
            return 1;
        } else {
            if (r1.getMatchedVol() == r2.getMatchedVol()) {
                return -Long.compare(Math.abs(r1.getSurplus()), Math.abs(r2.getSurplus()));
            } else {
                return -1;
            }
        }
    }

    public Optional<CalculationResult> calc(final Book book) {
        final int qty = (int) Double.parseDouble(book.param("LOTSIZE"));
        final int marketvolb = (int) Double.parseDouble(book.param("marketvolb")) / qty;
        final int marketvols = (int) Double.parseDouble(book.param("marketvols")) / qty;
        final int marketMax = Math.max(marketvolb, marketvols);
        final int guaranteedAsks = marketMax;
        long[] totalBids = BookUtils.totalBids(book.getBids(), marketMax);
        long[] totalAsks = BookUtils.totalBids(book.getAsks(), marketMax);
        double[] bidPrices = book.getBids().stream().mapToDouble(Order::getPrice).toArray();
        double[] askPrices = book.getAsks().stream().mapToDouble(Order::getPrice).toArray();

        List<CalculationResult> records = new ArrayList<>();
        for (int i = 0; i < bidPrices.length; i++) {
            int lastMatchedAskIndex = -1;
            while (lastMatchedAskIndex < (askPrices.length - 1) && askPrices[lastMatchedAskIndex + 1] <= bidPrices[i]) {
                lastMatchedAskIndex++;
            }
            if (lastMatchedAskIndex < askPrices.length && lastMatchedAskIndex >= 0) {
                long matchedVol = Math.min(totalBids[i], totalAsks[lastMatchedAskIndex]);
                long surplus = totalBids[i] - totalAsks[lastMatchedAskIndex];
                records.add(CalculationResult.builder()
                        .totalBids(totalBids[i])
                        .book(book)
                        .totalAsks(totalAsks[lastMatchedAskIndex])
                        .askPrice(askPrices[lastMatchedAskIndex])
                        .bidPrice(bidPrices[i])
                        .matchedVol(matchedVol)
                        .surplus(surplus)
                        .build()
                );
            } else {
                System.out.println(lastMatchedAskIndex);
            }
        }
        return records.stream().max(AuctionPriceCalculator::best);
    }


}
