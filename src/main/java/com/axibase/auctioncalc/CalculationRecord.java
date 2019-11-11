package com.axibase.auctioncalc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalculationRecord {
    private int accBids;
    private int bidCount;
    private double bidPrice;
    private double askPrice;
    private int askCount;
    private int accAskCount;
}
