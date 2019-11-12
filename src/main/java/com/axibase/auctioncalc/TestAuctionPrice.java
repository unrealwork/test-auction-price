package com.axibase.auctioncalc;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TestAuctionPrice {
    public static void main(String[] args) {
        Optional.ofNullable(Path.of(args[0]))
                .map(FileUtils::readBooks)
                .ifPresent(books -> FileUtils.writeBooks(books, Path.of(args[1])));
    }
}
