package pl.volleyflow.match.model.Set;

import java.util.UUID;

public record SetDto(
        UUID externalId,
        Integer setNumber,
        Integer homePoints,
        Integer awayPoints) {
}
