package pl.volleyflow.match.model;

import lombok.Builder;
import pl.volleyflow.club.model.ClubBasicDto;

import java.time.Instant;
import java.util.UUID;

@Builder
public record MatchDto(
        UUID externalId,
        ClubBasicDto createdByClub,
        MatchTeamDto homeTeam,
        MatchTeamDto awayTeam,
        MatchStatus status,
        Instant scheduledAt,
        Instant startedAt,
        Instant finishedAt,
        MatchLocation matchLocation) {
}
