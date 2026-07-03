package pl.volleyflow.personprofile.model;

public class PersonProfileMapper {

    public static PersonProfile createEntity(PersonProfileCreateRequest request) {
        return PersonProfile.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .displayName(request.displayName())
                .jumpCm(request.jumpCm())
                .build();
    }

    public static void updateEntity(PersonProfile personProfile, PersonProfileUpdateRequest request) {
        if (request.firstName() != null) {
            personProfile.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            personProfile.setLastName(request.lastName());
        }
        if (request.displayName() != null) {
            personProfile.setDisplayName(request.displayName());
        }
        if (request.jumpCm() != null) {
            personProfile.setJumpCm(request.jumpCm());
        }
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
