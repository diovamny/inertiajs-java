package com.example.pingcrm.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Pagination {

    public record Result(
        List<Map<String, Object>> data,
        int currentPage,
        int lastPage,
        int perPage,
        int total,
        Map<String, Object> links,
        Map<String, Object> filters
    ) {}

    public static Result of(String basePath, List<Map<String, Object>> items,
            long total, int page, int size, Map<String, Object> filters) {
        int totalPages = (int) Math.ceil((double) total / size);
        if (totalPages < 1) totalPages = 1;

        var links = new LinkedHashMap<String, Object>();
        links.put("first", basePath + "?page=1");
        links.put("last", basePath + "?page=" + totalPages);

        if (page > 1) {
            links.put("prev", basePath + "?page=" + (page - 1));
        }
        if (page < totalPages) {
            links.put("next", basePath + "?page=" + (page + 1));
        }

        var window = computeWindow(page, totalPages);
        links.put("window", window);

        return new Result(items, page, totalPages, size, (int) total, links, filters);
    }

    private static List<Integer> computeWindow(int current, int last) {
        int delta = 2;
        int start = Math.max(1, current - delta);
        int end = Math.min(last, current + delta);

        if (end - start < 2 * delta) {
            if (start == 1) {
                end = Math.min(last, start + 2 * delta);
            } else {
                start = Math.max(1, end - 2 * delta);
            }
        }

        var result = new java.util.ArrayList<Integer>();
        for (int i = start; i <= end; i++) {
            result.add(i);
        }
        return result;
    }
}
