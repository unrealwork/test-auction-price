package com.axibase.auctioncalc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AuctionPriceCalculator {
    public Optional<CalculationResult> calc(final Book book) {
        final int qty = (int) Double.parseDouble(book.param("LOTSIZE"));
        final int marketvolb = (int) Double.parseDouble(book.param("marketvolb")) / qty;
        final int marketvols = (int) Double.parseDouble(book.param("marketvols")) / qty;
        long[] totalBids = BookUtils.totalBids(book.getBids(), marketvolb);
        long[] totalAsks = BookUtils.totalBids(book.getAsks(), marketvols);
        double[] bidPrices = book.getBids().stream().mapToDouble(Order::getPrice).toArray();
        double[] askPrices = book.getAsks().stream().mapToDouble(Order::getPrice).toArray();
        
        List<CalculationResult> records = new ArrayList<>();
        for (int i = 0; i < bidPrices.length; i++) {
            int lastMatchedAskIndex = -1;
            while (lastMatchedAskIndex < (askPrices.length - 1) && askPrices[lastMatchedAskIndex + 1] <= bidPrices[i]) {
                lastMatchedAskIndex++;
            }
            if (lastMatchedAskIndex < askPrices.length && lastMatchedAskIndex > 0) {
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
            }
        }
        return records.stream()
                .min(Comparator.comparingLong(r -> Math.abs(r.getSurplus())));
    }


}
