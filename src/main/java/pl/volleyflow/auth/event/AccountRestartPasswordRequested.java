package pl.volleyflow.auth.event;

import java.util.UUID;

public record AccountRestartPasswordRequested(
        UUID eventId,
        UUID userAccountExternalId,
        String email,
        String token) {
}
