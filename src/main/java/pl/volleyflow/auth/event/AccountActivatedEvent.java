package pl.volleyflow.auth.event;

import java.util.UUID;

public record AccountActivatedEvent(
        UUID userExternalId) {
}
