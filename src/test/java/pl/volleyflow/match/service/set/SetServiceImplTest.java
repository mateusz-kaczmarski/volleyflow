package pl.volleyflow.match.service.set;

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
import pl.volleyflow.match.model.exceptions.SetNotFoundException;
import pl.volleyflow.match.model.match.MatchSide;
import pl.volleyflow.match.model.set.SetCreateRequest;
import pl.volleyflow.match.repository.MatchRepository;
import pl.volleyflow.match.repository.SetRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.service.UserAccountService;

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
class SetServiceImplTest {

    private static final String EMAIL = "user@example.com";
    private final UUID matchId = UUID.randomUUID();
    private final UUID clubId = UUID.randomUUID();
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

    @InjectMocks
    private SetServiceImpl setService;

    @Test
    void shouldRejectSetCreationWithoutStaffRole() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(new Match()));

        assertThrows(ClubMembershipAccessDeniedException.class,
                () -> setService.createSet(matchId, clubId, new SetCreateRequest(0, 0), EMAIL));

        verify(setRepository, never()).save(any());
    }

    @Test
    void shouldCreateNextSet() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        Match match = new Match();
        match.addTeam(MatchTeam.builder().club(club).side(MatchSide.HOME).build());
        match.addSet(SetEntity.builder().setNumber(3).build());
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(clubMembershipRepository.hasAnyRole(eq(2L), eq(1L), any())).thenReturn(true);

        var result = setService.createSet(matchId, clubId, new SetCreateRequest(25, 20), EMAIL);

        assertThat(result.setNumber()).isEqualTo(4);
        verify(setRepository).save(any(SetEntity.class));
    }

    @Test
    void shouldThrowWhenUpdatingUnknownSet() {
        Match match = new Match();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(new UserAccount()));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(setRepository.findByExternalIdAndMatch(setId, match)).thenReturn(Optional.empty());

        assertThrows(SetNotFoundException.class,
                () -> setService.updateSetVideo(matchId, clubId, setId, "https://example.com", EMAIL));

        verify(clubRepository, never()).findByExternalIdAndClubStatus(any(), any());
    }

    @Test
    void shouldTrimSetVideoUrl() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        Match match = new Match();
        match.addTeam(MatchTeam.builder().club(club).side(MatchSide.HOME).build());
        SetEntity set = SetEntity.builder().externalId(setId).setNumber(1).match(match).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(setRepository.findByExternalIdAndMatch(setId, match)).thenReturn(Optional.of(set));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(clubMembershipRepository.hasAnyRole(eq(2L), eq(1L), any())).thenReturn(true);

        var result = setService.updateSetVideo(matchId, clubId, setId, "  https://example.com/video  ", EMAIL);

        assertThat(result.videoUrl()).isEqualTo("https://example.com/video");
        assertThat(set.getVideoUrl()).isEqualTo("https://example.com/video");
    }

    @Test
    void shouldDeleteSet() {
        UserAccount user = UserAccount.builder().id(2L).build();
        Club club = Club.builder().id(1L).build();
        Match match = new Match();
        SetEntity set = SetEntity.builder().match(match).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(matchRepository.findByExternalId(matchId)).thenReturn(Optional.of(match));
        when(setRepository.findByExternalIdAndMatch(setId, match)).thenReturn(Optional.of(set));
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(clubMembershipRepository.hasAnyRole(eq(2L), eq(1L), any())).thenReturn(true);

        setService.deleteSet(matchId, clubId, setId, EMAIL);

        verify(setRepository).delete(set);
    }
}
