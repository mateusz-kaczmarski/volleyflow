package pl.volleyflow.match.model.setstatistics;

import java.util.List;

public record PlayerCareerStatisticsDto(
        int matchesPlayed,
        int setsPlayed,
        int points,
        AttackStatisticsDto attack,
        ReceptionStatisticsDto reception,
        ServeStatisticsDto serve,
        BlockStatisticsDto block,
        int defense,
        int assists,
        List<PlayerMatchStatisticsDto> matches) {
}
