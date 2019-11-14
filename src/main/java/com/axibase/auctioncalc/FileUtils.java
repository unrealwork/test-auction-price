package com.axibase.auctioncalc;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class FileUtils {
    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            .toFormatter()
            .withZone(ZoneId.of("Europe/Moscow"));

    public static List<Book> readBooks(final Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<String> logRecords = reader.lines().skip(2)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            final List<Book> books = new ArrayList<>();
            for (int i = 0; i < logRecords.size(); i += 4) {
                final Book b = parseBook(logRecords, i);

                final LocalDateTime date = b.getInstant()
                        .atZone(ZoneId.of("Europe/Moscow"))
                        .toLocalDateTime();

                if (date.getMinute() >= 40 && date.getMinute() < 50) {
                    books.add(b);
                }
            }
            return Collections.unmodifiableList(books);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Book parseBook(List<String> logRecords, int i) {
        final String timeDesc = logRecords.get(i);
        String[] parts = timeDesc.split(",INFO: ");

        final String paramsString = logRecords.get(i + 3);
        final Map<String, String> params = parseParams(paramsString);
        return new Book(parseDate(parts[0]),
                parts[1],
                parseOrders(logRecords.get(i + 1)),
                parseOrders(logRecords.get(i + 2)),
                params);
    }

    public static void writeBooks(final List<Book> books, final Path path) {
        final File file = path.toFile();
        if (file.exists() && file.isDirectory()) {
            Map<String, List<Book>> booksMap = books.stream().collect(Collectors.groupingBy(Book::getSecClass));
            booksMap.forEach((key, value) -> {
                final List<String> bds = value.stream().map(Book::getDescription).collect(Collectors.toList());
                try {
                    final Path logPath = Path.of(file.getAbsolutePath(), "auction_log_" + key + ".log");
                    Files.write(logPath, bds);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            });
        } else {
            throw new IllegalStateException(String.format("Invalid path: %s", path));
        }
    }


    private Instant parseDate(final String date) {
        return FMT.parse(date, Instant::from);
    }

    private static Map<String, String> parseParams(final String line) {
        return Arrays.stream(line.split("\\s"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
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
