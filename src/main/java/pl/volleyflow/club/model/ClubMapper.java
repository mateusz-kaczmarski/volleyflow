package pl.volleyflow.club.model;

import pl.volleyflow.clubmembership.model.ClubMemberDto;

import java.util.List;

public class ClubMapper {

    public static Club mapToEntity(ClubRequest clubRequest) {
        return mapToEntity(clubRequest, clubRequest.name());
    }

    public static Club mapToEntity(ClubRequest clubRequest, String normalizedName) {
        return Club.builder()
                .name(normalizedName)
                .avatar(clubRequest.avatar())
                .description(clubRequest.description())
                .build();
    }

    public static ClubBasicDto mapToDto(Club club) {
        return ClubBasicDto.builder()
                .externalId(club.getExternalId())
                .name(club.getName())
                .avatar(club.getAvatar())
                .description(club.getDescription())
                .build();
    }

    public static ClubBasicDto mapToDto(Club club, String userRole) {
        return ClubBasicDto.builder()
                .externalId(club.getExternalId())
                .name(club.getName())
                .avatar(club.getAvatar())
                .description(club.getDescription())
                .userRole(userRole)
                .build();
    }

    public static ClubDetailsDto mapToDetailsDto(Club club, String userRole, List<ClubMemberDto> members) {
        return ClubDetailsDto.builder()
                .externalId(club.getExternalId())
                .name(club.getName())
                .avatar(club.getAvatar())
                .description(club.getDescription())
                .userRole(userRole)
                .members(members)
                .build();
    }

    public static void updateEntity(Club club, ClubUpdateRequest request) {
        updateEntity(club, request, request.name());
    }

    public static void updateEntity(Club club, ClubUpdateRequest request, String normalizedName) {
        if (request.name() != null) {
            club.setName(normalizedName);
        }
        if (request.avatar() != null) {
            club.setAvatar(request.avatar());
        }
        if (request.description() != null) {
            club.setDescription(request.description());
        }
    }
}
