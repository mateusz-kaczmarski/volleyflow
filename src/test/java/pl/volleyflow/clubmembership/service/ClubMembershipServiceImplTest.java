package pl.volleyflow.clubmembership.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipCreateRequest;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.model.MemberPosition;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipAlreadyExistsException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipNotFoundException;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubMembershipServiceImplTest {

    private static final String EMAIL = "owner@example.com";
    private final UUID clubId = UUID.randomUUID();
    private final UUID membershipId = UUID.randomUUID();

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private PersonProfileRepository personProfileRepository;

    @Mock
    private ClubMembershipRepository membershipRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private ClubMembershipServiceImpl membershipService;

    @Test
    void shouldCreatePlayerMembership() {
        Club club = club();
        ClubMembershipCreateRequest request = playerRequest();
        PersonProfile profile = PersonProfile.builder().externalId(UUID.randomUUID())
                .firstName("Jan").lastName("Kowalski").build();
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(membershipRepository.hasRole(2L, 1L, ClubMembershipRole.OWNER)).thenReturn(true);
        when(personProfileRepository.save(any(PersonProfile.class))).thenReturn(profile);
        when(membershipRepository.save(any(ClubMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = membershipService.createMembership(request, EMAIL);

        assertThat(result.role()).isEqualTo(ClubMembershipRole.PLAYER);
        assertThat(result.clubExternalId()).isEqualTo(clubId);
        assertThat(result.personProfileExternalId()).isEqualTo(profile.getExternalId());
        verify(membershipRepository).save(any(ClubMembership.class));
    }

    @Test
    void shouldRejectCreationByNonOwner() {
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club()));
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));

        assertThrows(ClubMembershipAccessDeniedException.class,
                () -> membershipService.createMembership(playerRequest(), EMAIL));

        verify(personProfileRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicatePlayer() {
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club()));
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(membershipRepository.hasRole(2L, 1L, ClubMembershipRole.OWNER)).thenReturn(true);
        when(membershipRepository.existsPlayer(clubId, "Jan", "Kowalski", ClubStatus.ACTIVE)).thenReturn(true);

        assertThrows(ClubMembershipAlreadyExistsException.class,
                () -> membershipService.createMembership(playerRequest(), EMAIL));

        verify(personProfileRepository, never()).save(any());
    }

    @Test
    void shouldRequireMembershipToViewPlayers() {
        assertThrows(ClubMembershipAccessDeniedException.class,
                () -> membershipService.getPlayersByClub(clubId, EMAIL, true));

        verify(membershipRepository, never()).findByClubAndRoles(any(), any(), any(), any());
    }

    @Test
    void shouldGetPlayersWhenMember() {
        when(membershipRepository.isClubMember(clubId, EMAIL, ClubStatus.ACTIVE)).thenReturn(true);
        when(membershipRepository.findByClubAndRoles(
                clubId, List.of(ClubMembershipRole.PLAYER), true, ClubStatus.ACTIVE)).thenReturn(List.of());

        assertThat(membershipService.getPlayersByClub(clubId, EMAIL, true)).isEmpty();

        verify(membershipRepository).findByClubAndRoles(
                clubId, List.of(ClubMembershipRole.PLAYER), true, ClubStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenMembershipDoesNotExist() {
        when(membershipRepository.isClubMember(clubId, EMAIL, ClubStatus.ACTIVE)).thenReturn(true);

        assertThrows(ClubMembershipNotFoundException.class,
                () -> membershipService.getClubMembershipDetails(clubId, membershipId, EMAIL));
    }

    @Test
    void shouldNotDeleteOwnerMembership() {
        givenOwnerCanManage();
        when(membershipRepository.findActiveMembership(clubId, membershipId, ClubStatus.ACTIVE))
                .thenReturn(Optional.of(ClubMembership.builder().role(ClubMembershipRole.OWNER).build()));

        assertThrows(ClubMembershipAccessDeniedException.class,
                () -> membershipService.deleteMembership(clubId, membershipId, EMAIL));

        verify(membershipRepository, never()).deactivateMembership(any(), any(), any(), any());
    }

    @Test
    void shouldDeactivatePlayerMembership() {
        givenOwnerCanManage();
        when(membershipRepository.findActiveMembership(clubId, membershipId, ClubStatus.ACTIVE))
                .thenReturn(Optional.of(ClubMembership.builder().role(ClubMembershipRole.PLAYER).build()));
        when(membershipRepository.deactivateMembership(eq(clubId), eq(membershipId),
                eq(ClubStatus.ACTIVE), any(Instant.class))).thenReturn(1);

        membershipService.deleteMembership(clubId, membershipId, EMAIL);

        verify(membershipRepository).deactivateMembership(eq(clubId), eq(membershipId),
                eq(ClubStatus.ACTIVE), any(Instant.class));
    }

    private void givenOwnerCanManage() {
        when(clubRepository.findByExternalIdAndClubStatus(clubId, ClubStatus.ACTIVE)).thenReturn(Optional.of(club()));
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(membershipRepository.hasRole(2L, 1L, ClubMembershipRole.OWNER)).thenReturn(true);
    }

    private Club club() {
        return Club.builder().id(1L).externalId(clubId).build();
    }

    private UserAccount user() {
        return UserAccount.builder().id(2L).build();
    }

    private ClubMembershipCreateRequest playerRequest() {
        return new ClubMembershipCreateRequest(clubId, ClubMembershipRole.PLAYER,
                "Jan", "Kowalski", "Jan", 10, Set.of(MemberPosition.SETTER), null, null);
    }
}
