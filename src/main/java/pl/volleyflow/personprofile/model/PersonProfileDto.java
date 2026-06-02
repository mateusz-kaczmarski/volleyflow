package pl.volleyflow.personprofile.model;

import java.util.UUID;

public record PersonProfileDto(
        UUID externalId,
        UUID userAccountExternalId,
        String firstName,
        String lastName,
        String displayName,
        Integer jumpCm) {
}
