package pl.volleyflow.club.service;

import pl.volleyflow.club.model.ClubDto;
import pl.volleyflow.club.model.ClubRequest;

public interface ClubService {

    ClubDto createClub(ClubRequest clubRequest);

}
