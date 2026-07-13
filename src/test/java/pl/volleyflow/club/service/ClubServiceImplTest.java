package pl.volleyflow.club.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.volleyflow.club.model.*;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.model.ClubMemberDto;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.model.MemberPosition;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNoPermission;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceImplTest {

    private static final UUID USER_EXTERNAL_ID = UUID.fromString("27562246-72be-4fe5-a4ea-bc077e1c18df");
    private static final UUID CLUB_EXTERNAL_ID = UUID.fromString("c1652b12-e82b-411e-82b3-a1ab77d94123");
    private static final UUID OTHER_CLUB_EXTERNAL_ID = UUID.fromString("565a8f78-9c60-4b65-84a4-4a1916a34d32");
    private static final UUID PERSON_PROFILE_EXTERNAL_ID = UUID.fromString("c2690be0-7d8f-401e-bd09-581cfda5c258");
    private static final UUID MEMBERSHIP_EXTERNAL_ID = UUID.fromString("b01600a0-c8a9-442c-95a6-3b2fa98c3ca7");
    private static final String USER_EMAIL = "example@email.pl";

    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubMembershipRepository clubMembershipRepository;
    @Mock
    private PersonProfileService personProfileService;
    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private ClubServiceImpl clubService;

    @Test
    void throwClubAlreadyExistsWhenClubIsExists() {
        ClubRequest clubRequest = createClubRequest();

        when(clubRepository.existsByNameAndClubStatus("Club name", ClubStatus.ACTIVE)).thenReturn(true);

        assertThrows(ClubAlreadyExists.class, () -> clubService.createClub(clubRequest, USER_EMAIL));

        verify(userAccountService, never()).findByEmail(any());
        verify(clubRepository, never()).save(any());
        verify(personProfileService, never()).findByUserAccount(any());
    }

    @Test
    void throwUserNotFoundExceptionWhenCreateClub() {
        ClubRequest clubRequest = createClubRequest();

        when(clubRepository.existsByNameAndClubStatus("Club name", ClubStatus.ACTIVE)).thenReturn(false);
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> clubService.createClub(clubRequest, USER_EMAIL));

        verify(clubRepository, never()).save(any());
        verify(personProfileService, never()).findByUserAccount(any());
    }

    @Test
    void shouldCreateClub() {
        ClubBasicDto clubBasicDto = new ClubBasicDto(
                CLUB_EXTERNAL_ID,
                "Club name",
                "avatar.jpg",
                "description",
                "OWNER");

        when(clubRepository.existsByNameAndClubStatus("Club name", ClubStatus.ACTIVE)).thenReturn(false);
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.save(any(Club.class))).thenReturn(createClub());
        when(personProfileService.findByUserAccount(any())).thenReturn(Optional.of(createPersonProfile()));
        when(clubMembershipRepository.existsUserInClub(CLUB_EXTERNAL_ID, USER_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(false);

        assertThat(clubService.createClub(createClubRequest(), USER_EMAIL))
                .usingRecursiveComparison()
                .isEqualTo(clubBasicDto);

        verify(clubMembershipRepository).save(any(ClubMembership.class));
    }

    @Test
    void noUserFoundWhenGetMyClub() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> clubService.getMyClubs(USER_EMAIL));

        verify(clubMembershipRepository, never()).findActiveByUser(any(), any());
    }

    @Test
    void getMyClubs() {
        ClubBasicDto clubBasicDto = new ClubBasicDto(
                CLUB_EXTERNAL_ID,
                "Club name",
                "avatar.jpg",
                "description",
                "OWNER");

        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubMembershipRepository.findActiveByUser(USER_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(List.of(createClubMembership()));

        assertThat(clubService.getMyClubs(USER_EMAIL))
                .usingRecursiveComparison()
                .isEqualTo(List.of(clubBasicDto));
    }

    @Test
    void updateClub() {
        Club club = createClub();
        ClubUpdateRequest clubUpdateRequest = new ClubUpdateRequest("New name", "new-avatar.jpg", "new description");
        ClubBasicDto clubBasicDto = new ClubBasicDto(
                CLUB_EXTERNAL_ID,
                "New name",
                "new-avatar.jpg",
                "new description",
                "OWNER");

        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(club));
        when(clubMembershipRepository.hasRole(1L, 1L, ClubMembershipRole.OWNER)).thenReturn(true);
        when(clubRepository.findByNameAndClubStatus("New name", ClubStatus.ACTIVE)).thenReturn(Optional.empty());
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(clubService.updateClub(clubUpdateRequest, CLUB_EXTERNAL_ID, USER_EMAIL))
                .usingRecursiveComparison()
                .isEqualTo(clubBasicDto);

        verify(clubRepository).save(club);
    }

    @Test
    void throwUserNotFoundExceptionWhenUpdateClub() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> clubService.updateClub(createClubUpdateRequest(), CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubRepository, never()).findByExternalIdAndClubStatus(any(), any());
        verify(clubRepository, never()).save(any());
    }

    @Test
    void throwClubNotFoundExceptionWhenUpdateClub() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ClubNotFoundException.class, () -> clubService.updateClub(createClubUpdateRequest(), CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubMembershipRepository, never()).hasRole(anyLong(), anyLong(), any());
        verify(clubRepository, never()).save(any());
    }

    @Test
    void throwUserNoPermissionWhenUpdateClub() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(createClub()));
        when(clubMembershipRepository.hasRole(1L, 1L, ClubMembershipRole.OWNER)).thenReturn(false);

        assertThrows(UserNoPermission.class, () -> clubService.updateClub(createClubUpdateRequest(), CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubRepository, never()).save(any());
    }

    @Test
    void throwClubAlreadyExistsWhenUpdateClubNameIsTaken() {
        Club otherClub = createClub();
        otherClub.setExternalId(OTHER_CLUB_EXTERNAL_ID);

        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(createClub()));
        when(clubMembershipRepository.hasRole(1L, 1L, ClubMembershipRole.OWNER)).thenReturn(true);
        when(clubRepository.findByNameAndClubStatus("Club name", ClubStatus.ACTIVE)).thenReturn(Optional.of(otherClub));

        assertThrows(ClubAlreadyExists.class, () -> clubService.updateClub(createClubUpdateRequest(), CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubRepository, never()).save(any());
    }

    @Test
    void deleteClub() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(createClub()));
        when(clubMembershipRepository.hasRole(1L, 1L, ClubMembershipRole.OWNER)).thenReturn(true);

        clubService.deleteClub(CLUB_EXTERNAL_ID, USER_EMAIL);

        verify(clubRepository).updateStatus(eq(1L), eq(ClubStatus.ACTIVE), eq(ClubStatus.DELETED), any(Instant.class));
        verify(clubMembershipRepository).deactivateAllByClubId(eq(1L), any(Instant.class));
    }

    @Test
    void deleteClubNoFoundUser() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> clubService.deleteClub(CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubRepository, never()).findByExternalIdAndClubStatus(any(), any());
        verify(clubRepository, never()).updateStatus(anyLong(), any(), any(), any());
        verify(clubMembershipRepository, never()).deactivateAllByClubId(any(), any());
    }

    @Test
    void deleteClubNoClubFound() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ClubNotFoundException.class, () -> clubService.deleteClub(CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubRepository, never()).updateStatus(anyLong(), any(), any(), any());
        verify(clubMembershipRepository, never()).deactivateAllByClubId(any(), any());
    }

    @Test
    void deleteClubUserIsNoOwner() {
        when(userAccountService.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(createClub()));
        when(clubMembershipRepository.hasRole(1L, 1L, ClubMembershipRole.OWNER)).thenReturn(false);

        assertThrows(UserNoPermission.class, () -> clubService.deleteClub(CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubRepository, never()).updateStatus(anyLong(), any(), any(), any());
        verify(clubMembershipRepository, never()).deactivateAllByClubId(any(), any());
    }

    @Test
    void getClubThrowClubNotFoundException() {
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ClubNotFoundException.class, () -> clubService.getClub(CLUB_EXTERNAL_ID));
    }

    @Test
    void getClub() {
        ClubBasicDto clubBasicDto = new ClubBasicDto(
                CLUB_EXTERNAL_ID,
                "Club name",
                "avatar.jpg",
                "description",
                null);

        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(createClub()));

        assertThat(clubService.getClub(CLUB_EXTERNAL_ID))
                .usingRecursiveComparison()
                .isEqualTo(clubBasicDto);
    }

    @Test
    void throwClubNotFoundExceptionWhenGetClubDetails() {
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ClubNotFoundException.class, () -> clubService.getClubDetails(CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubMembershipRepository, never()).isClubMember(any(), any(), any());
    }

    @Test
    void throwUserNoPermissionWhenGetClubDetails() {
        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(createClub()));
        when(clubMembershipRepository.isClubMember(CLUB_EXTERNAL_ID, USER_EMAIL, ClubStatus.ACTIVE)).thenReturn(false);

        assertThrows(UserNoPermission.class, () -> clubService.getClubDetails(CLUB_EXTERNAL_ID, USER_EMAIL));

        verify(clubMembershipRepository, never()).findRole(any(), any(), any());
        verify(clubMembershipRepository, never()).findActiveByRole(any(), any(), any());
    }

    @Test
    void getClubDetails() {
        ClubMemberDto clubMemberDto = new ClubMemberDto(
                MEMBERSHIP_EXTERNAL_ID,
                PERSON_PROFILE_EXTERNAL_ID,
                ClubMembershipRole.PLAYER,
                "first name",
                "last name",
                "name",
                null,
                Set.of(MemberPosition.OPPOSITE),
                null,
                null,
                true);
        ClubDetailsDto clubDetailsDto = new ClubDetailsDto(
                CLUB_EXTERNAL_ID,
                "Club name",
                "avatar.jpg",
                "description",
                "OWNER",
                List.of(clubMemberDto));

        when(clubRepository.findByExternalIdAndClubStatus(CLUB_EXTERNAL_ID, ClubStatus.ACTIVE)).thenReturn(Optional.of(createClub()));
        when(clubMembershipRepository.isClubMember(CLUB_EXTERNAL_ID, USER_EMAIL, ClubStatus.ACTIVE)).thenReturn(true);
        when(clubMembershipRepository.findRole(CLUB_EXTERNAL_ID, USER_EMAIL, ClubStatus.ACTIVE)).thenReturn(Optional.of(ClubMembershipRole.OWNER));
        when(clubMembershipRepository.findActiveByRole(CLUB_EXTERNAL_ID, ClubMembershipRole.PLAYER, ClubStatus.ACTIVE)).thenReturn(List.of(createPlayerClubMembership()));

        assertThat(clubService.getClubDetails(CLUB_EXTERNAL_ID, USER_EMAIL))
                .usingRecursiveComparison()
                .isEqualTo(clubDetailsDto);
    }

    private ClubRequest createClubRequest() {
        return new ClubRequest("Club name", "avatar.jpg", "description");
    }

    private ClubUpdateRequest createClubUpdateRequest() {
        return new ClubUpdateRequest("Club name", "avatar.jpg", "description");
    }

    private UserAccount createUserAccount() {
        return UserAccount.builder()
                .phone("0000000000")
                .email(USER_EMAIL)
                .externalId(USER_EXTERNAL_ID)
                .emailVerified(true)
                .passwordHash("password")
                .id(1L)
                .build();
    }

    private Club createClub() {
        return Club.builder()
                .name("Club name")
                .avatar("avatar.jpg")
                .id(1L)
                .description("description")
                .externalId(CLUB_EXTERNAL_ID)
                .clubStatus(ClubStatus.ACTIVE)
                .build();
    }

    private PersonProfile createPersonProfile() {
        return PersonProfile.builder()
                .displayName("name")
                .id(1L)
                .lastName("last name")
                .firstName("first name")
                .externalId(PERSON_PROFILE_EXTERNAL_ID)
                .build();
    }

    private ClubMembership createClubMembership() {
        return ClubMembership.builder()
                .externalId(MEMBERSHIP_EXTERNAL_ID)
                .id(1L)
                .role(ClubMembershipRole.OWNER)
                .club(createClub())
                .active(true)
                .personProfile(createPersonProfile())
                .positions(Set.of(MemberPosition.OPPOSITE))
                .build();
    }

    private ClubMembership createPlayerClubMembership() {
        return ClubMembership.builder()
                .externalId(MEMBERSHIP_EXTERNAL_ID)
                .id(1L)
                .role(ClubMembershipRole.PLAYER)
                .club(createClub())
                .active(true)
                .personProfile(createPersonProfile())
                .positions(Set.of(MemberPosition.OPPOSITE))
                .build();
    }

}
