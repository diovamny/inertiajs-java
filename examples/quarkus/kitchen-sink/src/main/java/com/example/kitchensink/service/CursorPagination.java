package com.example.kitchensink.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Emulates Laravel's cursor pagination payloads:
 * {@code {data: [...], next_cursor, next_page_url, prev_cursor, prev_page_url}}.
 *
 * <p>The cursor is an opaque base64-encoded offset the client echoes back as
 * the {@code cursor} query parameter (mirrors Laravel's opaque cursors for
 * the demo data, which is static after seeding).</p>
 */
public final class CursorPagination {

    private CursorPagination() {
    }

    public static Map<String, Object> payload(String path, List<?> items,
            long offset, long pageSize, long total, Map<String, Object> query) {
        var map = new LinkedHashMap<String, Object>();
        map.put("data", new ArrayList<>(items));
        var nextOffset = offset + pageSize;
        var prevOffset = Math.max(0, offset - pageSize);
        map.put("next_cursor", nextOffset < total ? encode(nextOffset) : null);
        map.put("next_page_url", nextOffset < total ? url(path, query, nextOffset) : null);
        map.put("prev_cursor", offset > 0 ? encode(offset) : null);
        map.put("prev_page_url", offset > 0 ? url(path, query, prevOffset) : null);
        return map;
    }

    public static long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) return 0;
        try {
            var json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            var value = json.replaceAll("\\{\"offset\":(\\d+)}", "$1").trim();
            return value.matches("\\d+") ? Long.parseLong(value) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static String encode(long offset) {
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(("{\"offset\":" + offset + "}").getBytes(StandardCharsets.UTF_8));
    }

    private static String url(String path, Map<String, Object> query, long offset) {
        var sb = new StringBuilder(path).append("?");
        if (query != null) {
            for (var entry : query.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().toString().isBlank()) {
                    sb.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                        .append('&');
                }
            }
        }
        sb.append("cursor=").append(encode(offset));
        return sb.toString();
    }
}
