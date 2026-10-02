package pl.volleyflow.match.model.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SetStatisticInput(
        @NotNull UUID playerExternalId,
        @NotNull @Min(0) Integer points,
        @NotNull Integer balance,
        @NotNull @Min(0) Integer serveTotal,
        @NotNull @Min(0) Integer serveError,
        @NotNull @Min(0) Integer serveAces,
        @NotNull @Min(0) Integer receptionTotal,
        @NotNull @Min(0) Integer receptionError,
        @NotNull @Min(0) @Max(100) Integer receptionPositivePercent,
        @NotNull @Min(0) @Max(100) Integer receptionPerfectPercent,
        @NotNull @Min(0) Integer attackTotal,
        @NotNull @Min(0) Integer attackError,
        @NotNull @Min(0) Integer attackBlocked,
        @NotNull @Min(0) Integer attackPoints,
        @NotNull @Min(0) Integer blockPoints,
        @NotNull @Min(0) Integer blockTouches,
        @NotNull @Min(0) Integer defense,
        @NotNull @Min(0) Integer assists) {
}
