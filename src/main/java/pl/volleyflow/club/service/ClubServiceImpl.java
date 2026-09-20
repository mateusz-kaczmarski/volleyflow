package pl.volleyflow.club.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.club.model.*;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.model.ClubMemberDto;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipMapper;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.common.StringNormalizer;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNoPermission;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final PersonProfileService personProfileService;
    private final UserAccountService userAccountService;

    @Override
    @Transactional
    public ClubBasicDto createClub(ClubRequest clubRequest, String ownerEmail) {
        String normalizedName = StringNormalizer.trimRequired(clubRequest.name(), "club name");
        log.info("Starting club creation: name={}, ownerEmail={}", normalizedName, ownerEmail);

        if (clubRepository.existsByNameAndClubStatus(normalizedName, ClubStatus.ACTIVE)) {
            log.warn("Club creation rejected: active club with name {} already exists", normalizedName);
            throw new ClubAlreadyExists("Club with name " + normalizedName + " already exists");
        }

        UserAccount userAccount = userAccountService.findByEmail(ownerEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Club club = ClubMapper.mapToEntity(clubRequest, normalizedName);

        Club savedClub = clubRepository.save(club);
        createOwnerMembership(savedClub, userAccount);
        log.info("Created club: externalId={}, name={}, ownerEmail={}", savedClub.getExternalId(), savedClub.getName(), ownerEmail);

        return ClubMapper.mapToDto(savedClub, ClubMembershipRole.OWNER.name());
    }

    @Override
    public List<ClubBasicDto> getMyClubs(String userEmail) {
        UserAccount userAccount = userAccountService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return clubMembershipRepository.findActiveByUser(userAccount.getExternalId(), ClubStatus.ACTIVE)
                .stream()
                .map(membership -> ClubMapper.mapToDto(membership.getClub(), membership.getRole().name()))
                .toList();
    }

    @Override
    @Transactional
    public ClubBasicDto updateClub(ClubUpdateRequest clubUpdateRequest, UUID clubExternalId, String userEmail) {
        log.info("Starting club update: clubExternalId={}, userEmail={}", clubExternalId, userEmail);
        String normalizedName = StringNormalizer.trimOptionalButRejectBlank(clubUpdateRequest.name(), "club name");
        UserAccount userAccount = userAccountService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Club club = clubRepository.findByExternalIdAndClubStatus(clubExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        requireClubOwner(userAccount, club);

        if (normalizedName != null) {
            clubRepository.findByNameAndClubStatus(normalizedName, ClubStatus.ACTIVE)
                    .filter(existingClub -> !existingClub.getExternalId().equals(clubExternalId))
                    .ifPresent(existingClub -> {
                        log.warn("Club update rejected: name {} already used by clubExternalId={}",
                                normalizedName, existingClub.getExternalId());
                        throw new ClubAlreadyExists("Club with name " + normalizedName + " already exists");
                    });
        }

        ClubMapper.updateEntity(club, clubUpdateRequest, normalizedName);
        Club updatedClub = clubRepository.save(club);
        log.info("Updated club: clubExternalId={}, userEmail={}", updatedClub.getExternalId(), userEmail);
        return ClubMapper.mapToDto(updatedClub, ClubMembershipRole.OWNER.name());

    }

    @Override
    @Transactional
    public void deleteClub(UUID clubExternalId, String userEmail) {
        log.info("Starting club delete: clubExternalId={}, userEmail={}", clubExternalId, userEmail);
        UserAccount userAccount = userAccountService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Club club = clubRepository.findByExternalIdAndClubStatus(clubExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        requireClubOwner(userAccount, club);

        clubRepository.updateStatus(club.getId(), ClubStatus.ACTIVE, ClubStatus.DELETED, Instant.now());
        clubMembershipRepository.deactivateAllByClubId(club.getId(), Instant.now());

        log.info("Deleted club and deactivated memberships: clubExternalId={}, name={}, userEmail={}",
                clubExternalId, club.getName(), userEmail);
    }

    @Override
    public ClubBasicDto getClub(UUID clubExternalId) {
        return clubRepository.findByExternalIdAndClubStatus(clubExternalId, ClubStatus.ACTIVE)
                .map(ClubMapper::mapToDto)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
    }

    @Override
    public ClubDetailsDto getClubDetails(UUID clubExternalId, String userEmail) {
        Club club = clubRepository.findByExternalIdAndClubStatus(clubExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        if (!clubMembershipRepository.isClubMember(clubExternalId, userEmail, ClubStatus.ACTIVE)) {
            log.warn("Club details access denied: clubExternalId={}, userEmail={}", clubExternalId, userEmail);
            throw new UserNoPermission("User has no permission to watch this club");
        }

        String userRole = clubMembershipRepository.findRole(clubExternalId, userEmail, ClubStatus.ACTIVE)
                .map(Enum::name)
                .orElse(null);
        List<ClubMemberDto> members = clubMembershipRepository.findActiveByRole(
                        clubExternalId,
                        ClubMembershipRole.PLAYER,
                        ClubStatus.ACTIVE
                )
                .stream()
                .map(ClubMembershipMapper::mapToClubMemberDto)
                .toList();

        return ClubMapper.mapToDetailsDto(club, userRole, members);
    }

    @Override
    public List<ClubBasicDto> getClubsByName(String clubName, String userEmail) {
        UserAccount userAccount = userAccountService.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return clubRepository.findActiveByNameContaining(clubName.trim(), ClubStatus.ACTIVE)
                .stream()
                .map(ClubMapper::mapToDto)
                .toList();
    }


    private void createOwnerMembership(Club club, UserAccount userAccount) {
        PersonProfile personProfile = personProfileService.findByUserAccount(userAccount)
                .orElseThrow(() -> new UserNotFoundException("Person profile not found for user"));

        if (clubMembershipRepository.existsUserInClub(
                club.getExternalId(),
                userAccount.getExternalId(),
                ClubStatus.ACTIVE
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
        log.info("Created owner membership: clubExternalId={}, userExternalId={}",
                club.getExternalId(), userAccount.getExternalId());
    }

    private void requireClubOwner(UserAccount userAccount, Club club) {
        if (!clubMembershipRepository.hasRole(userAccount.getId(), club.getId(), ClubMembershipRole.OWNER)) {
            log.warn("Club owner permission denied: clubExternalId={}, userExternalId={}",
                    club.getExternalId(), userAccount.getExternalId());
            throw new UserNoPermission("User has no permission to edit this club");
        }
    }

}
