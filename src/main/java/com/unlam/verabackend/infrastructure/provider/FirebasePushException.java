package com.unlam.verabackend.infrastructure.provider;

public class FirebasePushException extends RuntimeException {
    private final boolean invalidToken;

    public FirebasePushException(String message, boolean invalidToken, Throwable cause) {
        super(message, cause);
        this.invalidToken = invalidToken;
    }

    public boolean isInvalidToken() {
        return invalidToken;
    }
}
