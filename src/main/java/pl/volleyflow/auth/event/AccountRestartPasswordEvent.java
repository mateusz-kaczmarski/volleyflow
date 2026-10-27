package pl.volleyflow.auth.event;

import java.util.UUID;

public record AccountRestartPasswordEvent(
        UUID userAccountExternalId,
        String email,
        String token) {
}
