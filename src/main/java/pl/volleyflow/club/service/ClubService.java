package pl.volleyflow.club.service;

import pl.volleyflow.club.model.ClubDto;
import pl.volleyflow.club.model.ClubRequest;

import java.util.List;
import java.util.UUID;

public interface ClubService {

    ClubDto createClub(ClubRequest clubRequest);

    List<ClubDto> getClubsByUser(UUID userExternalId);

}
