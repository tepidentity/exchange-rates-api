package com.zetta.exchangerates.testutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public final class TestUtils {
    private static final String BASE_DIR = "src/test/resources";
    private static final String JSON_DIR = "json";

    private TestUtils() {
        // static class
    }

    public static String buildExchangeResponse(Map<String, Double> entries) {
        return "{ \"data\" : { "
                + entries.entrySet()
                         .stream()
                         .map(e -> '"' + e.getKey() + "\" : " + e.getValue())
                         .collect(Collectors.joining(","))
                + "} }";
    }

    public static String jsonAsString(String file) throws IOException {
        validateFileName(file);
        return Files.readString(Paths.get(BASE_DIR, JSON_DIR, file));
    }

    private static void validateFileName(String file) {
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("File name is mandatory!");
        }
    }
}