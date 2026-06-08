package pl.volleyflow.clubmembership.model;

import pl.volleyflow.personprofile.model.PersonProfile;

import java.util.HashSet;

public class ClubMembershipMapper {

    public static PersonProfile mapToPersonProfile(ClubMembershipRequest request) {
        return PersonProfile.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .displayName(request.displayName())
                .build();
    }

    public static ClubMembership mapToEntity(ClubMembershipRequest request) {
        return ClubMembership.builder()
                .role(request.role())
                .shirtNumber(request.shirtNumber())
                .season(request.season())
                .positions(request.positions() == null ? new HashSet<>() : new HashSet<>(request.positions()))
                .activeFrom(request.activeFrom())
                .activeTo(request.activeTo())
                .build();
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
                .season(clubMembership.getSeason())
                .positions(clubMembership.getPositions())
                .activeFrom(clubMembership.getActiveFrom())
                .activeTo(clubMembership.getActiveTo())
                .active(clubMembership.isActive())
                .build();
    }
}
