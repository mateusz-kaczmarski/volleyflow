package pl.volleyflow.match.model.Match;

import lombok.Builder;
import pl.volleyflow.club.model.ClubBasicDto;

@Builder
public record MatchTeamDto(
        MatchSide side,
        ClubBasicDto club,
        int setsWon) {
}
