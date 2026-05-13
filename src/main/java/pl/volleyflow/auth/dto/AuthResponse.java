package pl.volleyflow.auth.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthResponse(
        UUID externalId,
        String email,
        String message) {
}
