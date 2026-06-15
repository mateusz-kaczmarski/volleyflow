package pl.volleyflow.club.service;

import pl.volleyflow.club.model.ClubBasicDto;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.model.ClubUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ClubService {

    ClubBasicDto createClub(ClubRequest clubRequest, String ownerEmail);

    List<ClubBasicDto> getClubsByUser(UUID userExternalId);

    List<ClubBasicDto> getMyClubs(String userEmail);

    ClubBasicDto updateClub(ClubUpdateRequest clubUpdateRequest, UUID clubExternalId, String userEmail);

    void deleteClub(UUID clubExternalId, String userEmail);

    ClubBasicDto getClub(UUID clubExternalId);

}
