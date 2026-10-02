package pl.volleyflow.match.service.setstatistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.entity.SetStatisticEntity;
import pl.volleyflow.match.model.Set.*;
import pl.volleyflow.match.model.exceptions.MatchNotFoundException;
import pl.volleyflow.match.model.exceptions.SetNotFoundException;
import pl.volleyflow.match.repository.MatchRepository;
import pl.volleyflow.match.repository.SetRepository;
import pl.volleyflow.match.repository.SetStatisticsRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class SetStatisticsServiceImpl implements SetStatisticsService {

    private final UserAccountService userAccountService;
    private final ClubRepository clubRepository;
    private final MatchRepository matchRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final SetRepository setRepository;
    private final SetStatisticsRepository setStatisticsRepository;

    @Transactional
    @Override
    public void saveSetStatistics(UUID matchExternalId,
                                  UUID clubExternalId,
                                  UUID setExternalId,
                                  SetStatisticsUpdateRequest setStatisticsUpdateRequest,
                                  String email) {
        UserAccount userAccount = getUserOrThrow(email);
        Match match = getMatchOrThrow(matchExternalId);
        SetEntity set = findSetOrThrow(setExternalId, match);
        Club club = getClubByExternalId(clubExternalId);

        requireStaffRole(userAccount, club);
        requireClubParticipatesInMatch(club, match);

        boolean participates = match.getTeams().stream()
                .anyMatch(team -> team.getClub().getId().equals(club.getId()));

        if (!participates) {
            throw new ClubMembershipAccessDeniedException("Club does not participate in this match");
        }

        long uniquePlayers = setStatisticsUpdateRequest.players().stream()
                .map(SetStatisticInput::playerExternalId)
                .distinct()
                .count();

        if (uniquePlayers != setStatisticsUpdateRequest.players().size()) {
            throw new IllegalArgumentException("Player occurs more than once");
        }

        List<ClubMembership> activeByRole = clubMembershipRepository.findActiveByRole(
                clubExternalId,
                ClubMembershipRole.PLAYER,
                ClubStatus.ACTIVE);

        Map<UUID, PersonProfile> activePlayers = new HashMap<>();

        for (ClubMembership clubMembership : activeByRole) {
            activePlayers.put(clubMembership.getPersonProfile().getExternalId(), clubMembership.getPersonProfile());
        }

        Map<UUID, SetStatisticEntity> existingStatistics = new HashMap<>();
        for (SetStatisticEntity statistic : setStatisticsRepository.findBySetAndClub(set, club)) {
            existingStatistics.put(statistic.getPlayer().getExternalId(), statistic);
        }

        List<SetStatisticEntity> statisticsToSave = new ArrayList<>();
        for (SetStatisticInput input : setStatisticsUpdateRequest.players()) {
            PersonProfile player = activePlayers.get(input.playerExternalId());
            if (player == null) {
                throw new IllegalArgumentException("Player is not an active member of this club");
            }

            SetStatisticEntity statistic = existingStatistics.remove(input.playerExternalId());
            if (statistic == null) {
                statistic = SetStatisticsMapper.mapToEntity(input, set, club, player);
            } else {
                SetStatisticsMapper.updateEntity(statistic, input);
            }
            statisticsToSave.add(statistic);
        }

        setStatisticsRepository.deleteAll(existingStatistics.values());
        setStatisticsRepository.saveAll(statisticsToSave);
    }

    @Transactional(readOnly = true)
    @Override
    public SetStatisticsDto getSetStatistics(UUID matchExternalId,
                                             UUID clubExternalId,
                                             UUID setExternalId,
                                             String email) {
        UserAccount userAccount = getUserOrThrow(email);
        Match match = getMatchOrThrow(matchExternalId);
        SetEntity set = findSetOrThrow(setExternalId, match);
        Club club = getClubByExternalId(clubExternalId);

        requireClubParticipatesInMatch(club, match);
        requireActiveMembership(userAccount, club);

        boolean participates = match.getTeams().stream()
                .anyMatch(team -> team.getClub().getId().equals(club.getId()));

        if (!participates) {
            throw new ClubMembershipAccessDeniedException("Club does not participate in this match");
        }

        List<SetStatisticDto> result = setStatisticsRepository.findBySetAndClub(set, club).stream()
                .map(SetStatisticsMapper::mapToDto)
                .toList();

        return new SetStatisticsDto(result);
    }

    private SetEntity findSetOrThrow(UUID setExternalId, Match match) {
        return setRepository.findByExternalIdAndMatch(setExternalId, match)
                .orElseThrow(() -> new SetNotFoundException("Set not found"));
    }

    private Match getMatchOrThrow(UUID matchExternalId) {
        return matchRepository.findByExternalId(matchExternalId)
                .orElseThrow(() -> new MatchNotFoundException("Match nor found"));
    }

    private UserAccount getUserOrThrow(String email) {
        return userAccountService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void requireStaffRole(UserAccount userAccount, Club club) {
        boolean canManageClub = clubMembershipRepository.hasAnyRole(
                userAccount.getId(),
                club.getId(),
                ClubMembershipRole.getStaffRoles()
        );

        if (!canManageClub) {
            log.warn("User {} cannot {}. Club id: {}",
                    userAccount.getId(), "create / update match for selected club", club.getId());
            throw new ClubMembershipAccessDeniedException("You cannot " + "create / update match for selected club");
        }
    }

    private Club getClubByExternalId(UUID request) {
        return clubRepository.findByExternalIdAndClubStatus(request, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
    }

    private void requireActiveMembership(UserAccount userAccount, Club club) {
        if (!clubMembershipRepository.hasActiveMembership(userAccount.getId(), club.getId())) {
            log.warn("User {} cannot view matches for club {}", userAccount.getId(), club.getId());
            throw new ClubMembershipAccessDeniedException("You cannot view matches for selected club");
        }
    }

    private void requireClubParticipatesInMatch(Club club, Match match) {
        boolean participates = match.getTeams().stream()
                .anyMatch(team -> team.getClub().getId().equals(club.getId()));

        if (!participates) {
            throw new ClubMembershipAccessDeniedException(
                    "Club does not participate in this match");
        }
    }

}
