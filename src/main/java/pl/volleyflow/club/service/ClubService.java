package pl.volleyflow.club.service;

import pl.volleyflow.club.model.ClubDto;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.model.ClubUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ClubService {

    ClubDto createClub(ClubRequest clubRequest, String ownerEmail);

    List<ClubDto> getClubsByUser(UUID userExternalId);

    List<ClubDto> getMyClubs(String email);

    ClubDto updateClub(ClubUpdateRequest clubUpdateRequest, UUID clubExternalId, String userEmail);



}
