package io.github.dg.quarkus.inertia.util;

/**
 * Safe JSON encoder for embedding JSON in HTML {@code <script>} tags.
 * <p>
 * Escapes characters that could break out of a script context or cause
 * parsing issues: {@code <}, {@code >}, {@code &}, U+2028 (line separator),
 * and U+2029 (paragraph separator).
 * </p>
 */
public final class SafeJsonEncoder {

    private SafeJsonEncoder() {}

    /**
     * Encode a JSON string for safe insertion into an HTML script tag.
     *
     * @param json the JSON string to encode
     * @return the encoded string safe for script context
     */
    public static String encodeForScript(String json) {
        if (json == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(json.length() + 16);
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            switch (c) {
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                case '/' -> sb.append("\\/");
                case '\u2028' -> sb.append("\\u2028");
                case '\u2029' -> sb.append("\\u2029");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
