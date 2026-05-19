package pl.volleyflow.user.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserAccountDto(
        UUID externalId,
        String email) {
}
