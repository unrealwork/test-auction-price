package com.axibase.auctioncalc;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Order {
    private final int count;
    private final double price;


    public double volume() {
        return count * price;
    }
}
