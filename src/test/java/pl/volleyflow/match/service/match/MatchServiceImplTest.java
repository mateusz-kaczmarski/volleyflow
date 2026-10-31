package pl.volleyflow.match.service.match;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.model.exceptions.MatchNotFoundException;
import pl.volleyflow.match.model.exceptions.MatchTeamsMustBeDifferentException;
import pl.volleyflow.match.model.exceptions.SetException;
import pl.volleyflow.match.model.match.MatchCreateRequest;
import pl.volleyflow.match.model.match.MatchLocation;
import pl.volleyflow.match.model.match.MatchStatus;
import pl.volleyflow.match.repository.MatchRepository;
import pl.volleyflow.match.repository.SetRepository;
import pl.volleyflow.match.repository.SetStatisticsRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.service.UserAccountService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

    private static final String EMAIL = "user@example.com";
    private final UUID clubId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();

    @Mock
    private UserAccountService userAccountService;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private ClubMembershipRepository clubMembershipRepository;
    @Mock
    private SetRepository setRepository;
    @Mock
    private SetStatisticsRepository setStatisticsRepository;

    @InjectMocks
    private MatchServiceImpl matchService;

    @Test
    void shouldRejectMatchWithSameTeams() {
        Club club = Club.builder().id(1L).externalId(clubId).build();
        UserAccount user = UserAccount.builder().id(2L).email(EMAIL).build();
        MatchCreateRequest request = new MatchCreateRequest(clubId, clubId, clubId,
                Instant.now().plusSeconds(3600), new MatchLocation("Warszawa", "00-001", "Sportowa", "1"), 2026);
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));

        assertThrows(MatchTeamsMustBeDifferentException.class, () -> matchService.createMatch(request, EMAIL));

        verify(matchRepository, never()).save(any());
    }

    @Test
    void shouldCreateMatchWithThreeInitialSets() {
        UUID awayId = UUID.randomUUID();
        Club home = Club.builder().id(1L).externalId(clubId).name("Home").build();
        Club away = Club.builder().id(2L).externalId(awayId).name("Away").build();
        UserAccount user = UserAccount.builder().id(3L).email(EMAIL).build();
        MatchCreateRequest request = new MatchCreateRequest(clubId, clubId, awayId,
                Instant.now().plusSeconds(3600), new MatchLocation("Warszawa", "00-001", "Sportowa", "1"), 2026);
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(home));
        when(clubRepository.findByExternalIdAndClubStatus(awayId, ClubStatus.ACTIVE)).thenReturn(Optional.of(away));
        when(clubMembershipRepository.hasAnyRole(eq(3L), eq(1L), any())).thenReturn(true);
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = matchService.createMatch(request, EMAIL);

        assertThat(result.sets()).hasSize(3);
        assertThat(result.homeTeam().club().name()).isEqualTo("Home");
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void shouldRequireMembershipToListMatches() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));

        assertThrows(ClubMembershipAccessDeniedException.class,
                () -> matchService.getClubMatches(clubId, EMAIL, null));

        verify(matchRepository, never()).findByCreatedByClubExternalIdOrderByScheduledAtAsc(any());
    }

    @Test
    void shouldFilterMatchesBySeason() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(clubMembershipRepository.hasActiveMembership(2L, 1L)).thenReturn(true);
        when(matchRepository.findByCreatedByClubExternalIdAndSeasonNumberOrderByScheduledAtAsc(clubId, 2026))
                .thenReturn(List.of());

        assertThat(matchService.getClubMatches(clubId, EMAIL, 2026)).isEmpty();

        verify(matchRepository).findByCreatedByClubExternalIdAndSeasonNumberOrderByScheduledAtAsc(clubId, 2026);
    }

    @Test
    void shouldThrowWhenFinishingUnknownMatch() {
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(new UserAccount()));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.empty());

        assertThrows(MatchNotFoundException.class, () -> matchService.finishMatch(matchId, EMAIL));
    }

    @Test
    void shouldRejectFinishWithoutThreeWonSets() {
        Match match = Match.builder().createdByClub(Club.builder().id(1L).build())
                .sets(List.of(SetEntity.builder().setNumber(1).homePoints(25).awayPoints(20).build())).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(UserAccount.builder().id(2L).build()));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(clubMembershipRepository.hasAnyRole(eq(2L), eq(1L), any())).thenReturn(true);

        assertThrows(SetException.class, () -> matchService.finishMatch(matchId, EMAIL));
    }

    @Test
    void shouldFinishMatchAfterThreeWonSets() {
        Match match = Match.builder().createdByClub(Club.builder().id(1L).build())
                .sets(List.of(
                        SetEntity.builder().setNumber(1).homePoints(25).awayPoints(20).build(),
                        SetEntity.builder().setNumber(2).homePoints(25).awayPoints(21).build(),
                        SetEntity.builder().setNumber(3).homePoints(25).awayPoints(22).build())).build();
        when(userAccountService.findByEmail(EMAIL))
                .thenReturn(Optional.of(UserAccount.builder().id(2L).email(EMAIL).build()));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(clubMembershipRepository.hasAnyRole(eq(2L), eq(1L), any())).thenReturn(true);

        matchService.finishMatch(matchId, EMAIL);

        assertThat(match.getStatus()).isEqualTo(MatchStatus.FINISHED);
    }
}
