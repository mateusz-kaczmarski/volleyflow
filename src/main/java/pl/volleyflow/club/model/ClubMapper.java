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
                .userRole(null)
                .build();
    }

    public static ClubDto mapToDto(Club club, String userRole) {
        return ClubDto.builder()
                .externalId(club.getExternalId())
                .name(club.getName())
                .avatar(club.getAvatar())
                .description(club.getDescription())
                .userRole(userRole)
                .build();
    }

    public static void updateEntity(Club club, ClubUpdateRequest request) {
        if (request.name() != null) {
            club.setName(request.name());
        }
        if (request.avatar() != null) {
            club.setAvatar(request.avatar());
        }
        if (request.description() != null) {
            club.setDescription(request.description());
        }
    }
}
