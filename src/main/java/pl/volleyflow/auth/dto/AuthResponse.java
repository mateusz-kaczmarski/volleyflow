package pl.volleyflow.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String token,
        String message,
        UUID externalId) {

    public static AuthResponse registerSuccess(UUID externalId) {
        return new AuthResponse(
                null,
                "User registered successfully, active your account by the email",
                externalId);
    }

    public static AuthResponse loginSuccess(String token, UUID externalId) {
        return new AuthResponse(token, "Login successfully", externalId);
    }

}
