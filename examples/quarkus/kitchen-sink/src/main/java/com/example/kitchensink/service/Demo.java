package com.example.kitchensink.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/** Small helpers mirroring Laravel's {@code now()} helpers and {@code random_int}. */
public final class Demo {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private Demo() {
    }

    /** Equivalent of Laravel {@code now()->toDateTimeString()}. */
    public static String now() {
        return DATE_TIME.format(Instant.now());
    }

    /** Equivalent of Laravel {@code now()->toTimeString()}. */
    public static String time() {
        return DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC)
            .format(Instant.now());
    }

    /** Equivalent of Laravel {@code now()->toISOString()}. */
    public static String iso() {
        return Instant.now().toString();
    }

    /** Equivalent of Laravel {@code random_int(min, max)}. */
    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /** Equivalent of Laravel {@code Arr::random([...])}. */
    public static String random(String... values) {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private static final String[] WORDS = {
        "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing",
        "elit", "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore",
        "et", "dolore", "magna", "aliqua", "enim", "ad", "minim", "veniam",
        "quis", "nostrud", "exercitation", "ullamco", "laboris", "nisi", "aliquip",
        "ex", "ea", "commodo", "consequat", "duis", "aute", "irure", "in",
        "reprehenderit", "voluptate", "velit", "esse", "cillum", "eu", "fugiat",
        "nulla", "pariatur", "excepteur", "sint", "occaecat", "cupidatat",
        "non", "proident", "sunt", "culpa", "qui", "officia", "deserunt"};

    /** Faker-style placeholder sentence. */
    public static String fakeSentence() {
        var rnd = ThreadLocalRandom.current();
        int length = rnd.nextInt(8, 16);
        var words = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) words.append(' ');
            words.append(WORDS[rnd.nextInt(WORDS.length)]);
        }
        words.setCharAt(0, Character.toUpperCase(words.charAt(0)));
        words.append('.');
        return words.toString();
    }

    /** Blocking sleep in milliseconds (endpoints are {@code @Blocking}). */
    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /** Blocking sleep in seconds (endpoints are {@code @Blocking}). */
    public static void sleepSeconds(int seconds) {
        sleepMillis(seconds * 1000L);
    }
}
