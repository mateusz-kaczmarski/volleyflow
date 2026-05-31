package pl.volleyflow.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String token,
        String message,
        UUID externalId) {

    public static AuthResponse success(UUID externalId) {
        return new AuthResponse(null, "Login successfully", externalId);
    }

}
