package com.axibase.auctioncalc;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuctionPriceCalculator {
    public double calc(final Book book) {
        long[] totalBids = totalBids(book.getBids(), false);
        long[] totalAsks = totalBids(book.getAsks(), false);
        double[] bidPrices = book.getBids().stream().mapToDouble(Order::getPrice).toArray();
        double[] askPrices = book.getAsks().stream().mapToDouble(Order::getPrice).toArray();
        int length = Math.min(totalAsks.length, totalBids.length);
        System.out.printf("#%8s %9s %9s %9s %9s %9s%n",
                "Total", "Size", "Bid", "Ask", "Size", "Total");
        for (int i = 0; i < length; i++) {
            System.out.printf("%9s %9s %9.2f %9.2f %9s %9s%n",
                    totalBids[i], book.getBids().get(i).getCount(), bidPrices[i], askPrices[i],
                    book.getAsks().get(i).getCount(), totalAsks[i]);
        }

        if (length < bidPrices.length) {
            for (int i = length; i < bidPrices.length; i++) {
                System.out.printf("%9s %9s %9.2f %30s %n",
                        totalBids[i], book.getBids().get(i).getCount(), bidPrices[i],"");
            }
        }
        if (length < askPrices.length) {
            for (int i = length; i < askPrices.length; i++) {
                System.out.printf("%30s %9.2f %9s %9s%n",
                        "",askPrices[i], book.getAsks().get(i).getCount(), totalAsks[i]);
            }
        }
        List<CalcRecord> records = new ArrayList<>();
        for (int i = 0; i < bidPrices.length; i++) {
            int lastMatchedAskIndex = -1;
            while (lastMatchedAskIndex < (askPrices.length - 1) && askPrices[lastMatchedAskIndex + 1] <= bidPrices[i]) {
                lastMatchedAskIndex++;
            }
            if (lastMatchedAskIndex < askPrices.length && lastMatchedAskIndex > 0) {
                long matchedVol = Math.min(totalBids[i], totalAsks[lastMatchedAskIndex]);
                long surplus = totalBids[i] - totalAsks[lastMatchedAskIndex];
                records.add(CalcRecord.builder()
                        .totalBids(totalBids[i])
                        .totalAsks(totalAsks[lastMatchedAskIndex])
                        .askPrice(askPrices[lastMatchedAskIndex])
                        .bidPrice(bidPrices[i])
                        .matchedVol(matchedVol)
                        .surplus(surplus)
                        .build()
                );
            }
        }
        final Optional<CalcRecord> rec = records.stream()
                .min((r1, r2) -> Long.compare(Math.abs(r1.surplus), Math.abs(r2.getSurplus())));
        double res = rec
                .map(CalcRecord::auctionPrice)
                .orElse(0d);
        if (rec.isPresent()) {
            final CalcRecord record = rec.get();
            System.out.printf("TOP 20 auction price: %f, Imbalance: %d %n, Total asks: %d, Total bids: %d", 
                    res, record.getSurplus(), record.getTotalAsks(), record.getTotalBids()
            );
        }
        return res;
    }

    @Builder
    @Getter
    private static class CalcRecord {
        private long totalBids;
        private int bidsSize;
        private double bidPrice;
        private double askPrice;
        private int askSize;
        private long totalAsks;
        private long matchedVol;
        private long surplus;

        public double auctionPrice() {
            return totalBids == matchedVol ? bidPrice : askPrice;
        }
    }

    private static long[] totalBids(List<Order> bids, boolean asc) {
        long[] result = new long[bids.size()];
        int start = asc ? bids.size() - 1 : 0;
        int end = asc ? -1 : bids.size();
        int step = asc ? -1 : 1;
        for (int i = start; i != end; i += step) {
            int count = bids.get(i).getCount();
            result[i] = i == start ? count : result[i - step] + count;
        }
        return result;
    }
}
