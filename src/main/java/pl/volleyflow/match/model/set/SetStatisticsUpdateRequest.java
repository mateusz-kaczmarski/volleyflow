package pl.volleyflow.match.model.set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetStatisticsUpdateRequest(
        @NotNull(message = "players cannot be null")
        List<@NotNull @Valid SetStatisticInput> players
) {
}
