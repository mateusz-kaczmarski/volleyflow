package pl.volleyflow.clubmembership.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.*;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipAlreadyExistsException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipNotFoundException;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ClubMembershipServiceImpl implements ClubMembershipService {

    private final ClubRepository clubRepository;
    private final PersonProfileRepository personProfileRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public ClubMembershipDto createMembership(ClubMembershipCreateRequest request, String userEmail) {
        log.info("Starting membership creation: clubExternalId={}, role={}, userEmail={}",
                request.clubExternalId(), request.role(), userEmail);

        Club club = clubRepository.findByExternalIdAndClubStatus(request.clubExternalId(), ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
        UserAccount userAccount = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        requireClubOwner(userAccount, club);
        validateCreateMembership(request);

        PersonProfile personProfile = ClubMembershipMapper.mapToPersonProfile(request);
        PersonProfile savedPersonProfile = personProfileRepository.save(personProfile);

        ClubMembership membership = ClubMembershipMapper.mapToEntity(request);
        membership.setClub(club);
        membership.setPersonProfile(savedPersonProfile);

        ClubMembership savedMembership = clubMembershipRepository.save(membership);
        log.info("Created membership: membershipExternalId={}, clubExternalId={}, role={}",
                savedMembership.getExternalId(), club.getExternalId(), savedMembership.getRole());

        return ClubMembershipMapper.mapToDto(savedMembership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembershipDto> getPlayersByClub(UUID clubExternalId,
                                                    String userEmail,
                                                    Boolean active) {
        requireClubMember(clubExternalId, userEmail);

        return clubMembershipRepository.findByClubAndRoles(
                        clubExternalId,
                        List.of(ClubMembershipRole.PLAYER),
                        active,
                        ClubStatus.ACTIVE
                )
                .stream()
                .map(ClubMembershipMapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClubMembershipDto getClubMembershipDetails(UUID clubExternalId,
                                                      UUID membershipExternalId,
                                                      String userEmail) {
        requireClubMember(clubExternalId, userEmail);

        ClubMembership membership = clubMembershipRepository
                .findActiveMembership(clubExternalId, membershipExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubMembershipNotFoundException("Club membership not found"));

        return ClubMembershipMapper.mapToDto(membership);
    }

    @Override
    @Transactional
    public ClubMembershipDto updateMembership(UUID clubExternalId,
                                              UUID membershipExternalId,
                                              ClubMembershipUpdateRequest request,
                                              String userEmail) {
        log.info("Starting membership update: clubExternalId={}, membershipExternalId={}, userEmail={}",
                clubExternalId, membershipExternalId, userEmail);
        Club club = clubRepository.findByExternalIdAndClubStatus(clubExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
        UserAccount userAccount = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        requireClubOwner(userAccount, club);

        ClubMembership membership = clubMembershipRepository
                .findActiveMembership(clubExternalId, membershipExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubMembershipNotFoundException("Club membership not found"));

        validateNotOwnerMembership(membership);
        validateUpdateMembership(clubExternalId, membershipExternalId, request);

        ClubMembershipMapper.updateEntity(membership, request);
        log.info("Updated membership: clubExternalId={}, membershipExternalId={}, role={}",
                clubExternalId, membershipExternalId, membership.getRole());
        return ClubMembershipMapper.mapToDto(membership);
    }

    @Override
    @Transactional
    public void deleteMembership(UUID clubExternalId, UUID membershipExternalId, String userEmail) {
        log.info("Starting membership delete: clubExternalId={}, membershipExternalId={}, userEmail={}",
                clubExternalId, membershipExternalId, userEmail);
        Club club = clubRepository.findByExternalIdAndClubStatus(clubExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
        UserAccount userAccount = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        requireClubOwner(userAccount, club);

        ClubMembership membership = clubMembershipRepository
                .findActiveMembership(clubExternalId, membershipExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubMembershipNotFoundException("Club membership not found"));
        validateNotOwnerMembership(membership);

        int updatedRows = clubMembershipRepository.deactivateMembership(
                clubExternalId,
                membershipExternalId,
                ClubStatus.ACTIVE,
                Instant.now()
        );
        if (updatedRows == 0) {
            log.warn("Membership delete failed: membership not found or inactive. clubExternalId={}, membershipExternalId={}",
                    clubExternalId, membershipExternalId);
            throw new ClubMembershipNotFoundException("Club membership not found");
        }
        log.info("Deleted membership: clubExternalId={}, membershipExternalId={}", clubExternalId, membershipExternalId);
    }

    @Override
    public List<ClubMembershipDto> getAllClubMembers(UUID clubExternalId, String userEmail, Boolean active) {
        Club club = clubRepository.findByExternalIdAndClubStatus(clubExternalId, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        UserAccount userAccount = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        requireClubStaff(userAccount, club);

        return clubMembershipRepository.findByClubAndRoles(
                        clubExternalId,
                        ClubMembershipRole.getAllRoles(),
                        active,
                        ClubStatus.ACTIVE
                )
                .stream()
                .map(ClubMembershipMapper::mapToDto)
                .toList();
    }

    private void requireClubMember(UUID clubExternalId, String userEmail) {
        if (!clubMembershipRepository.isClubMember(clubExternalId, userEmail, ClubStatus.ACTIVE)) {
            log.warn("Club membership access denied: clubExternalId={}, userEmail={}", clubExternalId, userEmail);
            throw new ClubMembershipAccessDeniedException("You do not have access to this club");
        }
    }

    private void requireClubOwner(UserAccount userAccount, Club club) {
        if (!clubMembershipRepository.hasRole(userAccount.getId(), club.getId(), ClubMembershipRole.OWNER)) {
            log.warn("Club membership management denied: clubExternalId={}, userExternalId={}",
                    club.getExternalId(), userAccount.getExternalId());
            throw new ClubMembershipAccessDeniedException("You cannot manage memberships in this club");
        }
    }

    private void requireClubStaff(UserAccount userAccount, Club club) {
        if (!clubMembershipRepository.hasAnyRole(userAccount.getId(), club.getId(), ClubMembershipRole.getStaffRoles())) {
            log.warn("Club members access denied: clubExternalId={}, userExternalId={}",
                    club.getExternalId(), userAccount.getExternalId());
            throw new ClubMembershipAccessDeniedException("You cannot view all members in this club");
        }
    }

    private void validateNotOwnerMembership(ClubMembership membership) {
        if (ClubMembershipRole.OWNER.equals(membership.getRole())) {
            log.warn("Membership change rejected: owner membership cannot be modified. membershipExternalId={}",
                    membership.getExternalId());
            throw new ClubMembershipAccessDeniedException("Owner membership cannot be modified");
        }
    }

    private void validateCreateMembership(ClubMembershipCreateRequest request) {
        validateMembershipRole(request.role());
        validateRoleDetails(request.role(), request.shirtNumber(), request.positions());
        validateMembershipDates(request.activeFrom(), request.activeTo());

        if (clubMembershipRepository.existsPlayer(
                request.clubExternalId(),
                request.firstName(),
                request.lastName(),
                ClubStatus.ACTIVE)) {
            log.warn("Membership creation rejected: player already exists. clubExternalId={}, firstName={}, lastName={}",
                    request.clubExternalId(), request.firstName(), request.lastName());
            throw new ClubMembershipAlreadyExistsException("Player already exists in this club");
        }

        if (ClubMembershipRole.PLAYER.equals(request.role())
                && request.shirtNumber() != null
                && clubMembershipRepository.existsShirtNumber(
                request.clubExternalId(),
                request.shirtNumber(),
                ClubStatus.ACTIVE)) {
            log.warn("Membership creation rejected: shirt number already exists. clubExternalId={}, shirtNumber={}",
                    request.clubExternalId(), request.shirtNumber());
            throw new ClubMembershipAlreadyExistsException("Shirt number already exists in this club");
        }
    }

    private void validateUpdateMembership(
            UUID clubExternalId,
            UUID membershipExternalId,
            ClubMembershipUpdateRequest request) {
        validateMembershipRole(request.role());
        validateRoleDetails(request.role(), request.shirtNumber(), request.positions());
        validateMembershipDates(request.activeFrom(), request.activeTo());

        if (clubMembershipRepository.existsPlayerExcept(
                clubExternalId,
                request.firstName(),
                request.lastName(),
                membershipExternalId,
                ClubStatus.ACTIVE)) {
            log.warn("Membership update rejected: player already exists. clubExternalId={}, membershipExternalId={}, firstName={}, lastName={}",
                    clubExternalId, membershipExternalId, request.firstName(), request.lastName());
            throw new ClubMembershipAlreadyExistsException("Player already exists in this club");
        }

        if (ClubMembershipRole.PLAYER.equals(request.role())
                && request.shirtNumber() != null
                && clubMembershipRepository.existsShirtNumberExcept(
                clubExternalId,
                request.shirtNumber(),
                membershipExternalId,
                ClubStatus.ACTIVE)) {
            log.warn("Membership update rejected: shirt number already exists. clubExternalId={}, membershipExternalId={}, shirtNumber={}",
                    clubExternalId, membershipExternalId, request.shirtNumber());
            throw new ClubMembershipAlreadyExistsException("Shirt number already exists in this club");
        }
    }

    private void validateMembershipRole(ClubMembershipRole role) {
        if (ClubMembershipRole.OWNER.equals(role)) {
            log.warn("Membership role validation rejected: OWNER cannot be assigned through membership endpoint");
            throw new ClubMembershipAccessDeniedException("Owner role cannot be assigned through membership endpoint");
        }
    }

    private void validateRoleDetails(ClubMembershipRole role, Integer shirtNumber, Set<MemberPosition> positions) {
        if (ClubMembershipRole.PLAYER.equals(role)) {
            validatePlayerDetails(positions);
            return;
        }

        validateNonPlayerDetails(shirtNumber, positions);
    }

    private void validatePlayerDetails(Set<MemberPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("Player must have at least one position");
        }
    }

    private void validateNonPlayerDetails(Integer shirtNumber, Set<MemberPosition> positions) {
        if (shirtNumber != null) {
            throw new IllegalArgumentException("Only player can have shirt number");
        }

        if (positions != null && !positions.isEmpty()) {
            throw new IllegalArgumentException("Only player can have positions");
        }
    }

    private void validateMembershipDates(LocalDate activeFrom, LocalDate activeTo) {
        if (activeFrom != null && activeTo != null && activeFrom.isAfter(activeTo)) {
            throw new IllegalArgumentException("activeFrom cannot be after activeTo");
        }
    }

}
