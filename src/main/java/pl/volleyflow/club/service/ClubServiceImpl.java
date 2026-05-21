package pl.volleyflow.club.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubAlreadyExists;
import pl.volleyflow.club.model.ClubDto;
import pl.volleyflow.club.model.ClubMapper;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.repository.ClubRepository;

@Service
@RequiredArgsConstructor
@Log4j2
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;

    @Override
    public ClubDto createClub(ClubRequest clubRequest) {
        log.info("Start register club {} ", clubRequest);

        if (clubRepository.existsByName(clubRequest.name())) {
            throw new ClubAlreadyExists("Club with name " + clubRequest.name() + " already exists");
        }

        Club club = ClubMapper.mapToEntity(clubRequest);

        clubRepository.save(club);
        log.info("Saved club {}", club);

        return ClubMapper.mapToDto(club);
    }

}
