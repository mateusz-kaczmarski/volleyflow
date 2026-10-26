package pl.volleyflow.match.model.set;

import java.util.UUID;

public record SetDto(
        UUID externalId,
        Integer setNumber,
        Integer homePoints,
        Integer awayPoints,
        String videoUrl) {
}
