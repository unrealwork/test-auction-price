package com.axibase.auctioncalc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Book {
    private AuctionPriceCalculator calculator = new AuctionPriceCalculator();
    private final Instant instant;
    private final String secClass;
    private final List<Order> bids;
    private final List<Order> asks;
    private final Map<String, String> params;

    public String param(final String paramName) {
        return params.get(paramName);
    }

    public String getDescription() {
        final int qty = (int) Double.parseDouble(param("LOTSIZE"));
        final int marketvolb = (int) Double.parseDouble(param("marketvolb")) / qty;
        final int marketvols = (int) Double.parseDouble(param("marketvols")) / qty;
        final int marketMax = Math.max(marketvolb, marketvols);
        final int guaranteedAsks = marketMax;
        long[] totalBids = BookUtils.totalBids(bids, marketMax);
        long[] totalAsks = BookUtils.totalBids(asks, marketMax);
        double[] bidPrices = this.getBids().stream().mapToDouble(Order::getPrice).toArray();
        double[] askPrices = this.getAsks().stream().mapToDouble(Order::getPrice).toArray();
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s - %s%n", 
                instant.atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime().toString(), secClass));

        builder.append(String.format("#%8s %9s %9s %9s %9s %9s%n",
                "Total", "Size", "Bid", "Ask", "Size", "Total"));
        builder.append(String.format("%9d %39s %9d %n",
                marketvolb, "", marketvols));
        int length = Math.min(totalAsks.length, totalBids.length);
        for (int i = 0; i < length; i++) {
            builder.append(String.format("%9s %9s %9.2f %9.2f %9s %9s%n",
                    totalBids[i], this.getBids().get(i).getCount(), bidPrices[i], askPrices[i],
                    this.getAsks().get(i).getCount(), totalAsks[i]));
        }

        if (length < bidPrices.length) {
            for (int i = length; i < bidPrices.length; i++) {
                builder.append(String.format("%9s %9s %9.2f %30s %n",
                        totalBids[i], this.getBids().get(i).getCount(), bidPrices[i], ""));
            }
        }
        if (length < askPrices.length) {
            for (int i = length; i < askPrices.length; i++) {
                builder.append(String.format("%30s %9.2f %9s %9s%n",
                        "", askPrices[i], this.getAsks().get(i).getCount(), totalAsks[i]));
            }
        }
        calculator.calc(this)
                .map(CalculationResult::description)
                .ifPresent(builder::append);
        builder.append(params.toString());
        return builder.toString();
    }


}
