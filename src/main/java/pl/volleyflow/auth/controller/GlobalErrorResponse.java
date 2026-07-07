package pl.volleyflow.auth.controller;

import org.springframework.http.HttpStatus;

import java.util.List;

public record GlobalErrorResponse(
        int httpStatus,
        String message,
        List<FieldErrorResponse> fieldErrors) {

    public static GlobalErrorResponse of(HttpStatus status, String message) {
        return new GlobalErrorResponse(status.value(), message, List.of());
    }

    public static GlobalErrorResponse validationError(List<FieldErrorResponse> fieldErrors) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new GlobalErrorResponse(status.value(), "Validation failed", fieldErrors);
    }

    public record FieldErrorResponse(
            String field,
            Object rejectedValue,
            String message) {
    }
}
