package com.sandustnetwork.fineclaim.claim.protection;

import java.util.Objects;

public final class ProtectionResult {

    private final boolean allowed;
    private final String message;

    private ProtectionResult(boolean allowed, String message) {
        this.allowed = allowed;
        this.message = message;
        if (!allowed) {
            Objects.requireNonNull(message, "message");
        }
    }

    public static ProtectionResult allowed() {
        return new ProtectionResult(true, "");
    }

    public static ProtectionResult denied(String message) {
        return new ProtectionResult(false, Objects.requireNonNull(message, "message"));
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String message() {
        return message;
    }
}
