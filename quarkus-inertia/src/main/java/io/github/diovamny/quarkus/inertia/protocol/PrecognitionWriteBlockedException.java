package io.github.diovamny.quarkus.inertia.protocol;

/**
 * Thrown when application code attempts a persistence write during a
 * precognition validate-only request (see {@link PrecognitionGuard}).
 */
public class PrecognitionWriteBlockedException extends IllegalStateException {

    public PrecognitionWriteBlockedException(String message) {
        super(message);
    }
}
