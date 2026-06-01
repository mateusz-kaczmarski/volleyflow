package pl.volleyflow.personprofile.model;

import java.util.UUID;

public record PersonProfileDto(
        UUID externalId,
        String firstName,
        String secondName,
        String displayName,
        int jumpCm) {
}
