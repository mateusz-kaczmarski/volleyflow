package pl.volleyflow.match.model;

import lombok.Builder;
import pl.volleyflow.club.model.ClubBasicDto;

@Builder
public record MatchTeamDto(
        MatchSide side,
        ClubBasicDto club,
        int setsWon) {
}
