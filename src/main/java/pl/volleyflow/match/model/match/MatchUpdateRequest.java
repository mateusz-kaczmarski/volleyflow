package pl.volleyflow.match.model.match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

public record MatchUpdateRequest(
        @NotNull(message = "scheduled time cannot be null")
        @FutureOrPresent(message = "scheduled time must be present or future")
        Instant scheduledAt,

        @Valid
        @NotNull(message = "match location cannot be null")
        MatchLocation location,

        @NotNull(message = "home club externalId cannot be null")
        UUID homeClubExternalId,

        @NotNull(message = "away club externalId cannot be null")
        UUID awayClubExternalId,

        @NotNull(message = "season cannot be null")
        @Positive(message = "season must be more then 0")
        Integer season) {
}
