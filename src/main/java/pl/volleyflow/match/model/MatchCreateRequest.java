package pl.volleyflow.match.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record MatchCreateRequest(
        @NotNull(message = "home club externalId cannot be null")
        UUID homeClubExternalId,

        @NotNull(message = "away club externalId cannot be null")
        UUID awayClubExternalId,

        @NotNull(message = "scheduled time cannot be null")
        @FutureOrPresent(message = "scheduled time must be present or future")
        Instant scheduledAt,

        @Valid
        @NotNull(message = "match location cannot be null")
        MatchLocation matchLocation) {
}
