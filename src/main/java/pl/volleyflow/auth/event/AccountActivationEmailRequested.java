package pl.volleyflow.auth.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AccountActivationEmailRequested(
        UUID eventId,
        UUID userExternalId,
        String email,
        String firstName,
        String lastName,
        String activationToken) {
}
