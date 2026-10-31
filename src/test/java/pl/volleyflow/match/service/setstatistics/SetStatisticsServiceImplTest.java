package pl.volleyflow.match.service.setstatistics;

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
import pl.volleyflow.match.entity.MatchTeam;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.model.match.MatchSide;
import pl.volleyflow.match.model.match.MatchStatus;
import pl.volleyflow.match.model.set.SetStatisticInput;
import pl.volleyflow.match.model.set.SetStatisticsUpdateRequest;
import pl.volleyflow.match.repository.MatchRepository;
import pl.volleyflow.match.repository.SetRepository;
import pl.volleyflow.match.repository.SetStatisticsRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.service.UserAccountService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetStatisticsServiceImplTest {

    private static final String EMAIL = "user@example.com";
    private final UUID clubId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();
    private final UUID setId = UUID.randomUUID();

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
    private SetStatisticsServiceImpl statisticsService;

    @Test
    void shouldRequireActiveMembershipToReadCareerStatistics() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));

        assertThrows(ClubMembershipAccessDeniedException.class,
                () -> statisticsService.getAllStatisticsForPlayer(clubId, UUID.randomUUID(), EMAIL));

        verifyNoInteractions(setStatisticsRepository);
    }

    @Test
    void shouldReturnEmptyCareerStatistics() {
        UUID playerId = UUID.randomUUID();
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(clubMembershipRepository.hasActiveMembership(2L, 1L)).thenReturn(true);
        when(setStatisticsRepository.findPlayerCareerStatistics(clubId, playerId, MatchStatus.FINISHED))
                .thenReturn(List.of());

        var result = statisticsService.getAllStatisticsForPlayer(clubId, playerId, EMAIL);

        assertThat(result.matches()).isEmpty();
        verify(setStatisticsRepository).findPlayerCareerStatistics(clubId, playerId, MatchStatus.FINISHED);
    }

    @Test
    void shouldRejectDuplicatePlayersBeforeSavingStatistics() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        Match match = new Match();
        match.addTeam(MatchTeam.builder().club(club).side(MatchSide.HOME).build());
        SetEntity set = SetEntity.builder().match(match).build();
        SetStatisticInput input = mock(SetStatisticInput.class);
        when(input.playerExternalId()).thenReturn(UUID.randomUUID());
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(setRepository.findByExternalIdAndMatch(setId, match)).thenReturn(Optional.of(set));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(clubMembershipRepository.hasAnyRole(eq(2L), eq(1L), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> statisticsService.saveSetStatistics(matchId, clubId, setId,
                        new SetStatisticsUpdateRequest(List.of(input, input)), EMAIL));

        verify(setStatisticsRepository, never()).saveAll(any());
    }

    @Test
    void shouldSaveEmptyStatisticsList() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        Match match = new Match();
        match.addTeam(MatchTeam.builder().club(club).side(MatchSide.HOME).build());
        SetEntity set = SetEntity.builder().match(match).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(setRepository.findByExternalIdAndMatch(setId, match)).thenReturn(Optional.of(set));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(clubMembershipRepository.hasAnyRole(eq(2L), eq(1L), any())).thenReturn(true);
        when(setStatisticsRepository.findBySetAndClub(set, club)).thenReturn(List.of());

        statisticsService.saveSetStatistics(matchId, clubId, setId,
                new SetStatisticsUpdateRequest(List.of()), EMAIL);

        verify(setStatisticsRepository).saveAll(List.of());
    }
}
