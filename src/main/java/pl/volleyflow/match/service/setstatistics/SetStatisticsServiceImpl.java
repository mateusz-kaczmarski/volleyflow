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
import pl.volleyflow.match.model.set.*;
import pl.volleyflow.match.model.exceptions.MatchNotFoundException;
import pl.volleyflow.match.model.exceptions.SetNotFoundException;
import pl.volleyflow.match.model.match.MatchSide;
import pl.volleyflow.match.model.match.MatchStatus;
import pl.volleyflow.match.model.setstatistics.*;
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

            SetStatisticInput calculatedInput = calculateStatistic(input);
            SetStatisticEntity statistic = existingStatistics.remove(input.playerExternalId());
            if (statistic == null) {
                statistic = SetStatisticsMapper.mapToEntity(calculatedInput, set, club, player);
            } else {
                SetStatisticsMapper.updateEntity(statistic, calculatedInput);
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

    @Transactional(readOnly = true)
    @Override
    public PlayerCareerStatisticsDto getAllStatisticsForPlayer(UUID clubExternalId,
                                                               UUID playerExternalId,
                                                               String email) {
        UserAccount userAccount = getUserOrThrow(email);
        Club club = getClubByExternalId(clubExternalId);
        requireActiveMembership(userAccount, club);

        List<SetStatisticEntity> statistics = setStatisticsRepository.findPlayerCareerStatistics(
                clubExternalId, playerExternalId, MatchStatus.FINISHED);
        Map<Match, List<SetStatisticEntity>> statisticsByMatch = new LinkedHashMap<>();
        for (SetStatisticEntity statistic : statistics) {
            statisticsByMatch.computeIfAbsent(statistic.getSet().getMatch(), ignored -> new ArrayList<>())
                    .add(statistic);
        }

        List<PlayerMatchStatisticsDto> matches = statisticsByMatch.entrySet().stream()
                .map(entry -> mapMatchStatistics(entry.getKey(), club, entry.getValue()))
                .toList();

        return new PlayerCareerStatisticsDto(
                statisticsByMatch.size(),
                statistics.size(),
                sum(statistics, SetStatisticEntity::getPoints),
                attackStatistics(statistics),
                receptionStatistics(statistics),
                serveStatistics(statistics),
                blockStatistics(statistics),
                sum(statistics, SetStatisticEntity::getDefense),
                sum(statistics, SetStatisticEntity::getAssists),
                matches);
    }

    private PlayerMatchStatisticsDto mapMatchStatistics(Match match,
                                                         Club club,
                                                         List<SetStatisticEntity> statistics) {
        String opponentName = match.getTeams().stream()
                .filter(team -> !team.getClub().getId().equals(club.getId()))
                .map(team -> team.getClub().getName())
                .findFirst()
                .orElse("Unknown opponent");

        int homeSets = (int) match.getSets().stream()
                .filter(set -> set.getHomePoints() > set.getAwayPoints())
                .count();
        int awaySets = (int) match.getSets().stream()
                .filter(set -> set.getAwayPoints() > set.getHomePoints())
                .count();
        if (match.getStatus() == MatchStatus.FINISHED && match.getSets().stream()
                .allMatch(set -> set.getHomePoints() == 0 && set.getAwayPoints() == 0)) {
            homeSets = match.getTeams().stream().filter(team -> team.getSide() == MatchSide.HOME)
                    .mapToInt(team -> team.getSetsWon()).findFirst().orElse(0);
            awaySets = match.getTeams().stream().filter(team -> team.getSide() == MatchSide.AWAY)
                    .mapToInt(team -> team.getSetsWon()).findFirst().orElse(0);
        }
        boolean clubIsHome = match.getTeams().stream()
                .anyMatch(team -> team.getClub().getId().equals(club.getId()) && team.getSide() == MatchSide.HOME);
        AttackStatisticsDto attack = attackStatistics(statistics);
        ReceptionStatisticsDto reception = receptionStatistics(statistics);
        ServeStatisticsDto serve = serveStatistics(statistics);

        return new PlayerMatchStatisticsDto(
                match.getExternalId(),
                match.getScheduledAt(),
                opponentName,
                clubIsHome ? homeSets + " : " + awaySets : awaySets + " : " + homeSets,
                statistics.size(),
                sum(statistics, SetStatisticEntity::getPoints),
                attack.successPercent(),
                attack.efficiencyPercent(),
                reception.positivePercent(),
                reception.perfectPercent(),
                serve.aces(),
                serve.efficiencyPercent(),
                sum(statistics, SetStatisticEntity::getBlockPoints),
                sum(statistics, SetStatisticEntity::getBlockTouches),
                sum(statistics, SetStatisticEntity::getDefense));
    }

    private AttackStatisticsDto attackStatistics(List<SetStatisticEntity> statistics) {
        int total = sum(statistics, SetStatisticEntity::getAttackTotal);
        int points = sum(statistics, SetStatisticEntity::getAttackPoints);
        int errors = sum(statistics, SetStatisticEntity::getAttackError);
        int blocked = sum(statistics, SetStatisticEntity::getAttackBlocked);
        return new AttackStatisticsDto(
                total,
                points,
                percent(points, total),
                percent(points - errors - blocked, total));
    }

    private ReceptionStatisticsDto receptionStatistics(List<SetStatisticEntity> statistics) {
        int total = sum(statistics, SetStatisticEntity::getReceptionTotal);
        return new ReceptionStatisticsDto(
                weightedPercent(statistics, SetStatisticEntity::getReceptionPositivePercent,
                        SetStatisticEntity::getReceptionTotal, total),
                weightedPercent(statistics, SetStatisticEntity::getReceptionPerfectPercent,
                        SetStatisticEntity::getReceptionTotal, total));
    }

    private ServeStatisticsDto serveStatistics(List<SetStatisticEntity> statistics) {
        int total = sum(statistics, SetStatisticEntity::getServeTotal);
        int aces = sum(statistics, SetStatisticEntity::getServeAces);
        int errors = sum(statistics, SetStatisticEntity::getServeError);
        return new ServeStatisticsDto(aces, percent(aces - errors, total));
    }

    private BlockStatisticsDto blockStatistics(List<SetStatisticEntity> statistics) {
        return new BlockStatisticsDto(
                sum(statistics, SetStatisticEntity::getBlockPoints),
                sum(statistics, SetStatisticEntity::getBlockTouches));
    }

    private SetStatisticInput calculateStatistic(SetStatisticInput input) {
        int serveTotal = input.serveError() + input.serveAces() + input.opponentReceivedServeCount();
        int receptionTotal = input.receptionError() + input.receptionNegativeCount()
                + input.receptionNeutralCount() + input.receptionPositiveCount() + input.receptionPerfectCount();
        int attackTotal = input.attackError() + input.attackBlocked()
                + input.attackPoints() + input.opponentDefendedAttackCount();
        int points = input.serveAces() + input.attackPoints() + input.blockPoints();

        return new SetStatisticInput(
                input.playerExternalId(),
                points,
                points - input.serveError() - input.receptionError() - input.attackError(),
                serveTotal,
                input.serveError(),
                input.serveAces(),
                input.opponentReceivedServeCount(),
                receptionTotal,
                input.receptionError(),
                input.receptionNegativeCount(),
                input.receptionNeutralCount(),
                input.receptionPositiveCount(),
                input.receptionPerfectCount(),
                percent(input.receptionPositiveCount() + input.receptionPerfectCount(), receptionTotal),
                percent(input.receptionPerfectCount(), receptionTotal),
                attackTotal,
                input.attackError(),
                input.attackBlocked(),
                input.attackPoints(),
                input.opponentDefendedAttackCount(),
                input.blockPoints(),
                input.blockTouches(),
                input.defense(),
                input.assists());
    }

    private int weightedPercent(List<SetStatisticEntity> statistics,
                                java.util.function.ToIntFunction<SetStatisticEntity> percentExtractor,
                                java.util.function.ToIntFunction<SetStatisticEntity> totalExtractor,
                                int total) {
        if (total == 0) {
            return 0;
        }
        long weightedTotal = statistics.stream()
                .mapToLong(statistic -> (long) percentExtractor.applyAsInt(statistic)
                        * totalExtractor.applyAsInt(statistic))
                .sum();
        return (int) Math.round((double) weightedTotal / total);
    }

    private int percent(int numerator, int denominator) {
        return denominator == 0 ? 0 : (int) Math.round((double) numerator * 100 / denominator);
    }

    private int sum(List<SetStatisticEntity> statistics,
                    java.util.function.ToIntFunction<SetStatisticEntity> extractor) {
        return statistics.stream().mapToInt(extractor).sum();
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
