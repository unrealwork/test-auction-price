package com.axibase.auctioncalc;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Map.entry;

@Data
@Builder
public class CalculationResult {
    private long totalBids;
    private int bidsSize;
    private double bidPrice;
    private double askPrice;
    private int askSize;
    private long totalAsks;
    private long matchedVol;
    private long surplus;
    private Book book;


    public double auctionPrice() {
        return (surplus > 0) ? Math.max(bidPrice, askPrice) : Math.min(bidPrice, askPrice);
    }

    public String description() {
        StringBuilder builder = new StringBuilder();
        final int qty = (int) Double.parseDouble(book.param("LOTSIZE"));
        final int marketvolb = (int) Double.parseDouble(book.param("marketvolb")) / qty;
        final int marketvols = (int) Double.parseDouble(book.param("marketvols")) / qty;

        final double auctionPrice = this.auctionPrice();
        builder.append(String.format("Calculation info: Price %9.2f; Imbalance: %d; Total bids: %d, Total asks: %d; %n",
                auctionPrice, this.getSurplus(), this.getTotalBids(), this.getTotalAsks()));

        final double lastPrice = Double.parseDouble(book.param("LAST"));
        final double serverAuctionPrice = Double.parseDouble(book.param("AUCTPRICE"));
        final double serverAuctValue = Double.parseDouble(book.param("auctvalue"));
        final long total = (long) Math.ceil(serverAuctValue / qty / serverAuctionPrice);
        builder.append(String.format("%20s %20s %20s%n",
                "Field",
                "SERVER",
                "TOP-20"));
        final LinkedHashMap<String, List<String>> table = new LinkedHashMap<>();
        Stream.of(
                entry("AuctionPrice,RUB", asList(
                        String.format("%.2f", serverAuctionPrice),
                        String.format("%.2f", auctionPrice))
                ),
                entry("AuctionVolume", asList(
                        String.format("%d", total),
                        String.format("%d", matchedVol))
                ),
                entry("LastPrice,RUB", asList(
                        String.format("%.2f", lastPrice),
                        String.format("%.2f", lastPrice))
                ),

                entry("MarketBids", asList(
                        String.format("%d", marketvolb),
                        String.format("%d", marketvolb))
                ),

                entry("MarketAsks", asList(
                        String.format("%d", marketvols),
                        String.format("%d", marketvols))
                ),
                entry("Imbalance,%", asList(
                        "",
                        String.format("%.2f", (double) surplus / matchedVol * 100))
                ),
                entry("MarketBids:%", asList(
                        String.format("%.3f", perc(marketvolb, total)),
                        String.format("%.3f", perc(marketvolb, matchedVol)))
                ),
                entry("MarketAsks:%", asList(
                        String.format("%.3f", perc(marketvols, total)),
                        String.format("%.3f", perc(marketvols, matchedVol)))
                ),
                entry("TotalValue:RUB", asList(
                        String.format("%.3f", serverAuctValue),
                        String.format("%.3f", matchedVol * qty * auctionPrice))
                ),
                entry("AuctionPriceDiff,%", asList(
                        String.format("%.3f", ((serverAuctionPrice - lastPrice) / lastPrice) * 100),
                        String.format("%.3f", ((auctionPrice - lastPrice) / lastPrice) * 100))
                )
        ).forEach(e -> table.put(e.getKey(), e.getValue()));
        
        builder.append(printResult(table));
        builder.append(String.format("%n"));
        if (Math.abs(auctionPrice - serverAuctionPrice) < 10e-5) {
            builder.append(String.format("Matched calculation!%n"));
        }
        return builder.toString();
    }


    private String printResult(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .map(e -> String.format("%20s %s", e.getKey(), e.getValue().stream().map(s -> String.format("%20s", s)).collect(Collectors.joining())))
                .collect(Collectors.joining(String.format("%n"), "", ""));
    }

    private static double perc(double part, double whole) {
        return part / whole * 100;
    }
}
