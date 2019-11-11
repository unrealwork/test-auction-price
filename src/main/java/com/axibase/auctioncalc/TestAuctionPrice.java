package com.axibase.auctioncalc;

import java.util.List;
import java.util.Optional;

public class TestAuctionPrice {
    public static void main(String[] args) {
        List<Book> books = Optional.ofNullable(TestAuctionPrice.class.getClassLoader().getResource("test.in"))
                .map(FileUtils::readBooks)
                .orElseThrow(IllegalStateException::new);
    }
}
