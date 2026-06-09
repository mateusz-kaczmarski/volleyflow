package pl.volleyflow.clubmembership.service;

import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.model.ClubMembershipRequest;

import java.util.List;
import java.util.UUID;

public interface ClubMembershipService {

    ClubMembershipDto createMembership(ClubMembershipRequest request);

    List<ClubMembershipDto> getPlayersByClub(UUID clubExternalId, String email);
}
