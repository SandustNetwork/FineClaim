package com.sandustnetwork.fineclaim.claim.application;

import java.util.Objects;

public record ClaimOperationResult(boolean success, String message) {

    public ClaimOperationResult {
        Objects.requireNonNull(message, "message");
    }

    public static ClaimOperationResult success(String message) {
        return new ClaimOperationResult(true, message);
    }

    public static ClaimOperationResult failure(String message) {
        return new ClaimOperationResult(false, message);
    }
}
