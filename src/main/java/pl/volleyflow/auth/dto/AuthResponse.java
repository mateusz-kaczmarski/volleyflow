package pl.volleyflow.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String token,
        String message,
        UUID externalId) {

    public static AuthResponse registerSuccess(UUID userExternalId) {
        return new AuthResponse(
                null,
                "User registered successfully, active your account by the email",
                userExternalId);
    }

    public static AuthResponse loginSuccess(String token, UUID userExternalId) {
        return new AuthResponse(token, "Login successfully", userExternalId);
    }

    public static AuthResponse activeFailed() {
        return new AuthResponse(null, "Cannot active your account", null);
    }

    public static AuthResponse withMessage(String message) {
        return new AuthResponse(null, message, null);
    }

    public static AuthResponse activeSuccessfully(String token, UUID userExternalId) {
        return new AuthResponse(token, "Your account has been activated", userExternalId);
    }

}
