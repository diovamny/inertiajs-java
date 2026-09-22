package io.github.diovamny.inertia.core.security;

/**
 * A rendered page object exceeded {@code inertia.max-page-bytes} (§3.17
 * scenario 11). Adapters translate this into {@code 413 Content Too Large}
 * instead of serving (or OOM-ing on) an unbounded payload. The message is
 * safe to log; renderers never include prop data in it.
 */
public class PageTooLargeException extends RuntimeException {

    private final long bytes;
    private final long maxBytes;

    public PageTooLargeException(long bytes, long maxBytes) {
        super("Inertia page too large: " + bytes + " bytes exceeds max-page-bytes=" + maxBytes);
        this.bytes = bytes;
        this.maxBytes = maxBytes;
    }

    /** Actual serialized size in bytes. */
    public long bytes() {
        return bytes;
    }

    /** Configured limit in bytes. */
    public long maxBytes() {
        return maxBytes;
    }
}
