package pl.volleyflow.clubmembership.service;

import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.model.ClubMembershipRequest;

public interface ClubMembershipService {

    ClubMembershipDto createMembership(ClubMembershipRequest request);
}
