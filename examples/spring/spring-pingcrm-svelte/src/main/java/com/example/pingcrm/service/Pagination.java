package com.example.pingcrm.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds Laravel-style pagination payloads: {@code {data: [...], links: [...]}}
 * where links follow the Tailwind window used by PingCRM's Pagination.vue.
 */
public final class Pagination {

    public static final int DEFAULT_SIZE = 10;

    private Pagination() {
    }

    public record Result(List<Object> data, List<Map<String, Object>> links) {
    }

    public static Result of(String path, List<?> items, long total, int page, int size,
            Map<String, Object> query) {
        var totalPages = Math.max(1, (int) Math.ceil(total / (double) size));
        var safePage = Math.min(Math.max(1, page), totalPages);

        var links = new ArrayList<Map<String, Object>>();
        links.add(link(url(path, safePage - 1, query), "&laquo; Previous", false));

        for (var p : window(safePage, totalPages)) {
            if (p == -1L) {
                links.add(link(null, "...", false));
            } else {
                links.add(link(url(path, p, query), String.valueOf(p), p == safePage));
            }
        }

        links.add(link(url(path, safePage + 1, query), "Next &raquo;", false));
        return new Result(new ArrayList<>(items), links);
    }

    private static List<Long> window(long current, long totalPages) {
        var pages = new ArrayList<Long>();
        if (totalPages <= 9) {
            for (long p = 1; p <= totalPages; p++) {
                pages.add(p);
            }
            return pages;
        }
        pages.add(1L);
        if (current > 4) pages.add(-1L);
        for (long p = Math.max(2, current - 2); p <= Math.min(totalPages - 1, current + 2); p++) {
            pages.add(p);
        }
        if (current < totalPages - 3) pages.add(-1L);
        pages.add(totalPages);
        return pages;
    }

    private static Map<String, Object> link(String url, String label, boolean active) {
        var link = new LinkedHashMap<String, Object>();
        link.put("url", url);
        link.put("label", label);
        link.put("active", active);
        return link;
    }

    private static String url(String path, long page, Map<String, Object> query) {
        if (page < 1) return null;
        var sb = new StringBuilder(path).append("?page=").append(page);
        if (query != null) {
            for (var entry : query.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().toString().isBlank()) {
                    sb.append('&').append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
                }
            }
        }
        return sb.toString();
    }
}
