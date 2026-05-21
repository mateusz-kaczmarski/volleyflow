package pl.volleyflow.club.model;

public class ClubMapper {

    public static Club mapToEntity(ClubRequest clubRequest) {
        return Club.builder()
                .name(clubRequest.name())
                .avatar(clubRequest.avatar())
                .description(clubRequest.description())
                .build();
    }

    public static ClubDto mapToDto(Club club) {
        return ClubDto.builder()
                .externalId(club.getExternalId())
                .name(club.getName())
                .avatar(club.getAvatar())
                .description(club.getDescription())
                .build();
    }
}
