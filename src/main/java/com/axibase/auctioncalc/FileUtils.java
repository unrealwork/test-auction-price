package com.axibase.auctioncalc;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class FileUtils {
    public static List<Book> readBooks(final URL path) {
        final AuctionPriceCalculator calculator = new AuctionPriceCalculator();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path.toURI()))) {
            List<String> logRecords = reader.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            final List<Book> books = new ArrayList<>();
            for (int i = 0; i < logRecords.size(); i += 4) {
                System.out.println(logRecords.get(i));
                final Book b = new Book(parseOrders(logRecords.get(i + 1)), parseOrders(logRecords.get(i + 2)));
                books.add(b);
                calculator.calc(b);
                System.out.println(logRecords.get(i + 3));
            }
            return Collections.unmodifiableList(books);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Order> parseOrders(String array) {
        final List<String> nums = Arrays.stream(array.replaceAll("[\\[\\]]", "").split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        if (nums.size() >= 2) {
            List<Order> result = new ArrayList<>();
            for (int i = 0; i < nums.size(); i += 2) {
                try {
                    final double price = Double.parseDouble(nums.get(i));
                    final int count = Integer.parseInt(nums.get(i + 1));
                    result.add(new Order(count, price));
                } catch (NumberFormatException e) {
                    throw new IllegalStateException(e);
                }

            }
            return Collections.unmodifiableList(result);
        }
        return Collections.emptyList();
    }
}
