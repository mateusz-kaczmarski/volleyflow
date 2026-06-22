package pl.volleyflow.clubmembership.service;

import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.model.ClubMembershipRequest;
import pl.volleyflow.clubmembership.model.ClubMembershipUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ClubMembershipService {

    ClubMembershipDto createMembership(ClubMembershipRequest request, String userEmail);

    List<ClubMembershipDto> getPlayersByClub(UUID clubExternalId, String userEmail, Boolean active);

    ClubMembershipDto getClubMembershipDetails(UUID clubExternalId, UUID memberExternalId, String userEmail);

    ClubMembershipDto updateMembership(UUID clubExternalId,
                                       UUID membershipExternalId,
                                       ClubMembershipUpdateRequest request,
                                       String userEmail);

    void deleteMembership(UUID clubExternalId, UUID membershipExternalId, String userEmail);

}
