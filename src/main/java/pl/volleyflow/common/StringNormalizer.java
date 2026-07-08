package pl.volleyflow.common;

import java.util.Locale;

public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeOptionalEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("email cannot be blank");
        }
        return normalizedEmail;
    }

    public static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public static String trimRequired(String value, String fieldName) {
        String normalizedValue = value.trim();
        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return normalizedValue;
    }

    public static String trimOptionalButRejectBlank(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        return trimRequired(value, fieldName);
    }
}
