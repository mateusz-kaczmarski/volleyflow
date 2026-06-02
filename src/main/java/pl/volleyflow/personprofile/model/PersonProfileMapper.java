package pl.volleyflow.personprofile.model;

public class PersonProfileMapper {

    public static PersonProfile mapToEntity(PersonProfileRequest request) {
        return PersonProfile.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .displayName(request.displayName())
                .jumpCm(request.jumpCm())
                .build();
    }

    public static PersonProfileDto mapToDto(PersonProfile personProfile) {
        return new PersonProfileDto(
                personProfile.getExternalId(),
                personProfile.getUserAccount() != null ? personProfile.getUserAccount().getExternalId() : null,
                personProfile.getFirstName(),
                personProfile.getLastName(),
                personProfile.getDisplayName(),
                personProfile.getJumpCm()
        );
    }
}
