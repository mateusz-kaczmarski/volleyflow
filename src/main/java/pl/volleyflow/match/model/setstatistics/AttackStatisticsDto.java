package pl.volleyflow.match.model.setstatistics;

public record AttackStatisticsDto(
        int total,
        int points,
        int successPercent,
        int efficiencyPercent) {
}
