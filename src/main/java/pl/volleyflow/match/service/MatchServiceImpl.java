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
import pl.volleyflow.match.model.exceptions.MatchTeamsMustBeDifferentException;
import pl.volleyflow.match.repository.MatchRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

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

        log.info("Creating match for clubs. Home club id: {}, away club id: {}", homeClub.getId(), awayClub.getId());

        validateDifferentClubs(homeClub, awayClub);

        Club createdByClub = getClubCreatingMatch(userAccount, homeClub, awayClub);

        MatchEntity match = MatchMapper.mapToEntity(request, homeClub, awayClub, userAccount, createdByClub);
        MatchEntity savedMatch = matchRepository.save(match);

        log.info("Successfully created match {} by user {} for club {}",
                savedMatch.getExternalId(), userAccount.getId(), createdByClub.getId());

        return MatchMapper.mapToDto(savedMatch);
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

    private Club getClubCreatingMatch(UserAccount userAccount, Club homeClub, Club awayClub) {
        boolean canManageHomeClub = clubMembershipRepository.hasAnyRole(
                userAccount.getId(),
                homeClub.getId(),
                ClubMembershipRole.getStaffRoles()
        );

        boolean canManageAwayClub = clubMembershipRepository.hasAnyRole(
                userAccount.getId(),
                awayClub.getId(),
                ClubMembershipRole.getStaffRoles()
        );

        log.info("User {} match creation permissions. Can manage home club: {}, can manage away club: {}",
                userAccount.getId(), canManageHomeClub, canManageAwayClub);

        if (!canManageHomeClub && !canManageAwayClub) {
            log.warn("User {} cannot create match for clubs {} and {}",
                    userAccount.getId(), homeClub.getId(), awayClub.getId());
            throw new ClubMembershipAccessDeniedException("You cannot create match for selected clubs");
        }

        if (canManageHomeClub) {
            return homeClub;
        }

        return awayClub;
    }

}
