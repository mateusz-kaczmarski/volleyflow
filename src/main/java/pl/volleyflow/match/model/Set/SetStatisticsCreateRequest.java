package pl.volleyflow.match.model.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetStatisticsCreateRequest(
        @NotNull(message = "players cannot be null")
        List<@NotNull @Valid SetStatisticInput> players
) {
}
