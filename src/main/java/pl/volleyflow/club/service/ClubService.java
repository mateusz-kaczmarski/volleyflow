package pl.volleyflow.club.service;

import pl.volleyflow.club.model.*;

import java.util.List;
import java.util.UUID;

public interface ClubService {

    ClubBasicDto createClub(ClubRequest clubRequest, String ownerEmail);

    List<ClubBasicDto> getMyClubs(String userEmail);

    ClubBasicDto updateClub(ClubUpdateRequest clubUpdateRequest, UUID clubExternalId, String userEmail);

    void deleteClub(UUID clubExternalId, String userEmail);

    ClubBasicDto getClub(UUID clubExternalId);

    ClubDetailsDto getClubDetails(UUID clubExternalId, String userEmail);

    List<ClubBasicDto> getClubsByName(String clubName, String userEmail);

}
