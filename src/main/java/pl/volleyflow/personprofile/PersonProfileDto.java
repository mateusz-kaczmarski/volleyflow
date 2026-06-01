package pl.volleyflow.personprofile;

import java.util.UUID;

public record PersonProfileDto(
        UUID externalId,
        String firstName,
        String secondName,
        String displayName,
        int jumpCm) {
}
