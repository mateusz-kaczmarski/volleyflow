package pl.volleyflow.match.model;

import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubMapper;
import pl.volleyflow.match.entity.MatchAddress;
import pl.volleyflow.match.entity.MatchEntity;
import pl.volleyflow.match.entity.MatchTeam;
import pl.volleyflow.user.entity.UserAccount;

public class MatchMapper {

    public static MatchEntity mapToEntity(MatchCreateRequest request, Club homeClub, Club awayClub,
                                          UserAccount createdBy, Club createdByClub) {
        MatchEntity match = MatchEntity.builder()
                .scheduledAt(request.scheduledAt())
                .location(mapToAddress(request.matchLocation()))
                .createdBy(createdBy)
                .createdByClub(createdByClub)
                .build();

        match.addTeam(mapToTeam(homeClub, MatchSide.HOME));
        match.addTeam(mapToTeam(awayClub, MatchSide.AWAY));

        return match;
    }

    public static MatchDto mapToDto(MatchEntity match) {
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
                .build();
    }

    private static MatchTeam mapToTeam(Club club, MatchSide side) {
        return MatchTeam.builder()
                .club(club)
                .side(side)
                .build();
    }

    private static MatchAddress mapToAddress(MatchLocation location) {
        return MatchAddress.builder()
                .city(location.city())
                .zipCode(location.zipCode())
                .street(location.street())
                .buildingNumber(location.buildingNumber())
                .build();
    }

    private static MatchTeamDto findTeamDto(MatchEntity match, MatchSide side) {
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

    private static MatchLocation mapToLocation(MatchEntity match) {
        MatchAddress location = match.getLocation();
        return MatchLocation.builder()
                .city(location.getCity())
                .zipCode(location.getZipCode())
                .street(location.getStreet())
                .buildingNumber(location.getBuildingNumber())
                .build();
    }
}
