package pl.volleyflow.club.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.volleyflow.club.model.*;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNoPermission;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final PersonProfileService personProfileService;
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

        Club savedClub = clubRepository.save(club);
        createOwnerMembership(savedClub, userAccount);
        log.info("Saved club {}", savedClub);

        return ClubMapper.mapToDto(savedClub);
    }

    @Override
    public List<ClubDto> getClubsByUser(UUID userExternalId) {
        return clubMembershipRepository.findAllByPersonProfileUserAccountExternalId(userExternalId).stream()
                .map(membership -> ClubMapper.mapToDto(membership.getClub(), membership.getRole().name()))
                .toList();
    }

    @Override
    public List<ClubDto> getMyClubs(String email) {
        UserAccount userAccount = userAccountService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return clubMembershipRepository.findAllByPersonProfileUserAccountExternalId(userAccount.getExternalId()).stream()
                .map(membership -> ClubMapper.mapToDto(membership.getClub(), membership.getRole().name()))
                .toList();
    }

    @Override
    public ClubDto updateClub(ClubUpdateRequest clubUpdateRequest, UUID clubExternalId, String userEmail) {
        UserAccount userAccount = userAccountService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Club club = clubRepository.findByExternalId(clubExternalId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        if (!clubMembershipRepository.isOwnerClub(userAccount.getId(), club.getId())) {
            throw new UserNoPermission("User have no permission to edit this club");
        }

        ClubMapper.updateEntity(club, clubUpdateRequest);
        Club updatedClub = clubRepository.save(club);
        return ClubMapper.mapToDto(updatedClub);

    }

    private void createOwnerMembership(Club club, UserAccount userAccount) {
        PersonProfile personProfile = personProfileService.findByUserAccount(userAccount)
                .orElseThrow(() -> new UserNotFoundException("Person profile not found for user"));

        if (clubMembershipRepository.existsByClubExternalIdAndPersonProfileUserAccountExternalId(
                club.getExternalId(),
                userAccount.getExternalId()
        )) {
            return;
        }

        ClubMembership clubMembership = ClubMembership.builder()
                .club(club)
                .personProfile(personProfile)
                .role(ClubMembershipRole.OWNER)
                .active(true)
                .build();

        clubMembershipRepository.save(clubMembership);
    }

}
