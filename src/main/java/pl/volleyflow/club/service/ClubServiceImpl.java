package pl.volleyflow.club.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.volleyflow.club.model.*;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final UserAccountService userAccountService;

    @Override
    public ClubDto createClub(ClubRequest clubRequest, String ownerEmail) {
        log.info("Start register club {} ", clubRequest);

        if (clubRepository.existsByName(clubRequest.name())) {
            throw new ClubAlreadyExists("Club with name " + clubRequest.name() + " already exists");
        }

        UserAccount userAccount = userAccountService.findByEmail(ownerEmail).
                orElseThrow(() -> new UserNotFoundException("User not found"));

        Club club = ClubMapper.mapToEntity(clubRequest);
        club.setOwner(userAccount);

        clubRepository.save(club);
        log.info("Saved club {}", club);

        return ClubMapper.mapToDto(club);
    }

    @Override
    public List<ClubDto> getClubsByUser(UUID userExternalId) {
        return clubRepository.findAllByUserExternalId(userExternalId).stream()
                .map(ClubMapper::mapToDto)
                .toList();
    }

}
