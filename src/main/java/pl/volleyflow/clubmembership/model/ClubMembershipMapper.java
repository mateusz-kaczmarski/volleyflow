package pl.volleyflow.clubmembership.model;

import pl.volleyflow.personprofile.model.PersonProfile;

import java.util.HashSet;

public class ClubMembershipMapper {

    public static PersonProfile mapToPersonProfile(ClubMembershipCreateRequest request) {
        return PersonProfile.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .displayName(request.displayName())
                .build();
    }

    public static ClubMembership mapToEntity(ClubMembershipCreateRequest request) {
        return ClubMembership.builder()
                .role(request.role())
                .shirtNumber(request.shirtNumber())
                .positions(request.positions() == null ? new HashSet<>() : new HashSet<>(request.positions()))
                .activeFrom(request.activeFrom())
                .activeTo(request.activeTo())
                .build();
    }

    public static void updateEntity(ClubMembership clubMembership, ClubMembershipUpdateRequest request) {
        clubMembership.setRole(request.role());
        clubMembership.setShirtNumber(request.shirtNumber());
        clubMembership.setPositions(request.positions() == null ? new HashSet<>() : new HashSet<>(request.positions()));
        clubMembership.setActiveFrom(request.activeFrom());
        clubMembership.setActiveTo(request.activeTo());

        PersonProfile personProfile = clubMembership.getPersonProfile();
        personProfile.setFirstName(request.firstName());
        personProfile.setLastName(request.lastName());
        personProfile.setDisplayName(request.displayName());
    }

    public static ClubMembershipDto mapToDto(ClubMembership clubMembership) {
        return ClubMembershipDto.builder()
                .externalId(clubMembership.getExternalId())
                .clubExternalId(clubMembership.getClub().getExternalId())
                .personProfileExternalId(clubMembership.getPersonProfile().getExternalId())
                .role(clubMembership.getRole())
                .firstName(clubMembership.getPersonProfile().getFirstName())
                .lastName(clubMembership.getPersonProfile().getLastName())
                .displayName(clubMembership.getPersonProfile().getDisplayName())
                .shirtNumber(clubMembership.getShirtNumber())
                .positions(new HashSet<>(clubMembership.getPositions()))
                .activeFrom(clubMembership.getActiveFrom())
                .activeTo(clubMembership.getActiveTo())
                .active(clubMembership.isActive())
                .build();
    }

    public static ClubMemberDto mapToClubMemberDto(ClubMembership clubMembership) {
        return ClubMemberDto.builder()
                .externalId(clubMembership.getExternalId())
                .personProfileExternalId(clubMembership.getPersonProfile().getExternalId())
                .role(clubMembership.getRole())
                .firstName(clubMembership.getPersonProfile().getFirstName())
                .lastName(clubMembership.getPersonProfile().getLastName())
                .displayName(clubMembership.getPersonProfile().getDisplayName())
                .shirtNumber(clubMembership.getShirtNumber())
                .positions(new HashSet<>(clubMembership.getPositions()))
                .activeFrom(clubMembership.getActiveFrom())
                .activeTo(clubMembership.getActiveTo())
                .active(clubMembership.isActive())
                .build();
    }
}
