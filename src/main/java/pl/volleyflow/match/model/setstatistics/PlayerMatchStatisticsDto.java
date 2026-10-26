package pl.volleyflow.match.model.setstatistics;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

public record PlayerMatchStatisticsDto(
        UUID matchExternalId,
        Instant scheduledAt,
        String opponentName,
        String result,
        int setsPlayed,
        int points,
        int attackSuccessPercent,
        int attackEfficiencyPercent,
        int receptionPositivePercent,
        int receptionPerfectPercent,
        int aces,
        int serveEfficiencyPercent,
        int blockPoints,
        int blockTouches,
        int defense) {
}
