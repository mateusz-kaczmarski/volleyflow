package pl.volleyflow.match.model.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SetCreateRequest(
        @NotNull(message = "home points cannot be null")
        @Min(value = 0, message = "home points cannot be negative")
        Integer homePoints,

        @NotNull(message = "away points cannot be null")
        @Min(value = 0, message = "away points cannot be negative")
        Integer awayPoints
) {
}
