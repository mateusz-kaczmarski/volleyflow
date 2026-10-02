package pl.volleyflow.match.model.Match;

import lombok.Builder;
import pl.volleyflow.club.model.ClubBasicDto;
import pl.volleyflow.match.model.Set.SetDto;

import java.time.Instant;
import java.util.List;
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
        UUID mvpPlayerExternalId,
        MatchLocation matchLocation,
        List<SetDto> sets) {
}
