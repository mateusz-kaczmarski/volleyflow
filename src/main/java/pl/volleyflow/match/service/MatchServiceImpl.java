package pl.volleyflow.match.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.match.entity.MatchEntity;
import pl.volleyflow.match.model.MatchCreateRequest;
import pl.volleyflow.match.model.MatchDto;
import pl.volleyflow.match.model.MatchMapper;
import pl.volleyflow.match.model.MatchUpdateRequest;
import pl.volleyflow.match.model.exceptions.MatchNotFoundException;
import pl.volleyflow.match.model.exceptions.MatchTeamsMustBeDifferentException;
import pl.volleyflow.match.repository.MatchRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class MatchServiceImpl implements MatchService {

    private final UserAccountService userAccountService;
    private final ClubRepository clubRepository;
    private final MatchRepository matchRepository;
    private final ClubMembershipRepository clubMembershipRepository;

    @Transactional
    @Override
    public MatchDto createMatch(MatchCreateRequest request, String email) {
        UserAccount userAccount = userAccountService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        log.info("Creating match requested by user {}. Home club externalId: {}, away club externalId: {}, scheduled at: {}",
                userAccount.getEmail(), request.homeClubExternalId(), request.awayClubExternalId(), request.scheduledAt());

        Club homeClub = getOrElseThrow(request.homeClubExternalId());

        Club awayClub = getOrElseThrow(request.awayClubExternalId());

        Club createdByClub = getOrElseThrow(request.createdByClubExternalId());

        log.info("Creating match for clubs. Home club id: {}, away club id: {}", homeClub.getId(), awayClub.getId());

        validateDifferentClubs(homeClub, awayClub);
        validateClubParticipatesInMatch(createdByClub, homeClub, awayClub);
        requireStaffRole(userAccount, createdByClub);

        MatchEntity match = MatchMapper.mapToEntity(request, homeClub, awayClub, userAccount, createdByClub);
        MatchEntity savedMatch = matchRepository.save(match);

        log.info("Successfully created match {} by user {} for club {}",
                savedMatch.getExternalId(), userAccount.getId(), createdByClub.getId());

        return MatchMapper.mapToDto(savedMatch);
    }

    @Transactional
    @Override
    public MatchDto updateMatch(UUID matchExternalId, MatchUpdateRequest request, String email) {
        UserAccount userAccount = userAccountService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        log.info("Updating match requested by user {}. Home club externalId: {}, away club externalId: {}, scheduled at: {}",
                userAccount.getEmail(), request.homeClubExternalId(), request.awayClubExternalId(), request.scheduledAt());

        MatchEntity match = matchRepository.findByExternalId(matchExternalId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found"));

        requireStaffRole(userAccount, match.getCreatedByClub());

        Club homeClub = getOrElseThrow(request.homeClubExternalId());
        Club awayClub = getOrElseThrow(request.awayClubExternalId());

        validateDifferentClubs(homeClub, awayClub);
        validateClubParticipatesInMatch(match.getCreatedByClub(), homeClub, awayClub);

        log.info("Updating match for clubs. Home club id: {}, away club id: {}", homeClub.getId(), awayClub.getId());

        MatchMapper.updateEntity(match, request, homeClub, awayClub);
        MatchEntity savedMatch = matchRepository.save(match);

        log.info("Successfully update match {} by user {} for club {}",
                savedMatch.getExternalId(), userAccount.getId(), savedMatch.getCreatedByClub().getId());

        return MatchMapper.mapToDto(savedMatch);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MatchDto> getClubMatches(UUID clubExternalId, String email) {
        UserAccount userAccount = userAccountService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Club club = getOrElseThrow(clubExternalId);

        requireActiveMembership(userAccount, club);

        return matchRepository.findByCreatedByClubExternalIdOrderByScheduledAtAsc(clubExternalId).stream()
                .map(MatchMapper::mapToDto)
                .toList();
    }

    private Club getOrElseThrow(UUID request) {
        return clubRepository.findByExternalIdAndClubStatus(request, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
    }

    private void validateDifferentClubs(Club homeClub, Club awayClub) {
        if (homeClub.getId().equals(awayClub.getId())) {
            throw new MatchTeamsMustBeDifferentException("Match teams must be different");
        }
    }

    private void validateClubParticipatesInMatch(Club club, Club homeClub, Club awayClub) {
        if (!club.getId().equals(homeClub.getId()) && !club.getId().equals(awayClub.getId())) {
            throw new IllegalArgumentException("Created by club must participate in match");
        }
    }

    private void requireStaffRole(UserAccount userAccount, Club club) {
        boolean canManageClub = clubMembershipRepository.hasAnyRole(
                userAccount.getId(),
                club.getId(),
                ClubMembershipRole.getStaffRoles()
        );

        if (!canManageClub) {
            log.warn("User {} cannot {}. Club id: {}", userAccount.getId(), "create / update match for selected club", club.getId());
            throw new ClubMembershipAccessDeniedException("You cannot " + "create / update match for selected club");
        }
    }

    private void requireActiveMembership(UserAccount userAccount, Club club) {
        if (!clubMembershipRepository.hasActiveMembership(userAccount.getId(), club.getId())) {
            log.warn("User {} cannot view matches for club {}", userAccount.getId(), club.getId());
            throw new ClubMembershipAccessDeniedException("You cannot view matches for selected club");
        }
    }

}
