package pl.volleyflow.match.model.Match;

import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubMapper;
import pl.volleyflow.match.entity.MatchAddress;
import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.entity.MatchTeam;
import pl.volleyflow.match.model.Set.SetMapper;
import pl.volleyflow.user.entity.UserAccount;

import java.util.Comparator;

public final class MatchMapper {

    private MatchMapper() {
    }

    public static Match mapToEntity(MatchCreateRequest request,
                                    Club homeClub,
                                    Club awayClub,
                                    UserAccount createdBy,
                                    Club createdByClub) {
        Match match = Match.builder()
                .scheduledAt(request.scheduledAt())
                .location(mapToAddress(request.matchLocation()))
                .createdBy(createdBy)
                .createdByClub(createdByClub)
                .build();

        match.addTeam(mapToTeam(homeClub, MatchSide.HOME));
        match.addTeam(mapToTeam(awayClub, MatchSide.AWAY));

        return match;
    }

    public static void updateEntity(Match match,
                                    MatchUpdateRequest request,
                                    Club homeClub,
                                    Club awayClub) {
        match.setScheduledAt(request.scheduledAt());
        match.setLocation(mapToAddress(request.location()));
        updateTeam(match, homeClub, MatchSide.HOME);
        updateTeam(match, awayClub, MatchSide.AWAY);
    }

    public static MatchDto mapToDto(Match match) {
        return MatchDto.builder()
                .externalId(match.getExternalId())
                .createdByClub(ClubMapper.mapToDto(match.getCreatedByClub()))
                .homeTeam(findTeamDto(match, MatchSide.HOME))
                .awayTeam(findTeamDto(match, MatchSide.AWAY))
                .status(match.getStatus())
                .scheduledAt(match.getScheduledAt())
                .startedAt(match.getStartedAt())
                .finishedAt(match.getFinishedAt())
                .matchLocation(mapToLocation(match))
                .sets(match.getSets().stream()
                        .sorted(Comparator.comparing(SetEntity::getSetNumber))
                        .map(SetMapper::mapToDto)
                        .toList())
                .build();
    }

    private static MatchTeam mapToTeam(Club club, MatchSide side) {
        return MatchTeam.builder()
                .club(club)
                .side(side)
                .build();
    }

    private static void updateTeam(Match match, Club club, MatchSide side) {
        match.getTeams().stream()
                .filter(team -> side.equals(team.getSide()))
                .findFirst()
                .ifPresentOrElse(
                        team -> team.setClub(club),
                        () -> match.addTeam(mapToTeam(club, side))
                );
    }

    private static MatchAddress mapToAddress(MatchLocation location) {
        return MatchAddress.builder()
                .city(location.city())
                .zipCode(location.zipCode())
                .street(location.street())
                .buildingNumber(location.buildingNumber())
                .build();
    }

    private static MatchTeamDto findTeamDto(Match match, MatchSide side) {
        return match.getTeams().stream()
                .filter(team -> side.equals(team.getSide()))
                .findFirst()
                .map(MatchMapper::mapToTeamDto)
                .orElse(null);
    }

    private static MatchTeamDto mapToTeamDto(MatchTeam team) {
        return MatchTeamDto.builder()
                .side(team.getSide())
                .club(ClubMapper.mapToDto(team.getClub()))
                .setsWon(team.getSetsWon())
                .build();
    }

    private static MatchLocation mapToLocation(Match match) {
        MatchAddress location = match.getLocation();
        return MatchLocation.builder()
                .city(location.getCity())
                .zipCode(location.getZipCode())
                .street(location.getStreet())
                .buildingNumber(location.getBuildingNumber())
                .build();
    }
}
