package pl.volleyflow.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String token,
        String message,
        UUID externalId) {

    public static AuthResponse registerSuccess(String token, UUID externalId) {
        return new AuthResponse(token, "User registered successfully", externalId);
    }

    public static AuthResponse loginSuccess(String token, UUID externalId) {
        return new AuthResponse(token, "Login successfully", externalId);
    }

}
