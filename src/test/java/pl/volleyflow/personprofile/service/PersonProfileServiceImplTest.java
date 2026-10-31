package pl.volleyflow.personprofile.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.model.PersonProfileAlreadyExistsException;
import pl.volleyflow.personprofile.model.PersonProfileCreateRequest;
import pl.volleyflow.personprofile.model.PersonProfileInUseException;
import pl.volleyflow.personprofile.model.PersonProfileNotFoundException;
import pl.volleyflow.personprofile.model.PersonProfileUpdateRequest;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonProfileServiceImplTest {

    private static final String EMAIL = "user@example.com";

    @Mock
    private PersonProfileRepository personProfileRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private ClubMembershipRepository clubMembershipRepository;

    @InjectMocks
    private PersonProfileServiceImpl personProfileService;

    @Test
    void shouldCreateProfileForUser() {
        UserAccount user = UserAccount.builder().externalId(UUID.randomUUID()).build();
        PersonProfileCreateRequest request = new PersonProfileCreateRequest("Jan", "Kowalski", "Jan", 40);

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(personProfileRepository.findByUserAccount(user)).thenReturn(Optional.empty());
        when(personProfileRepository.save(any(PersonProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = personProfileService.createProfile(request, EMAIL);

        assertThat(result.firstName()).isEqualTo("Jan");
        assertThat(result.userAccountExternalId()).isEqualTo(user.getExternalId());
        verify(personProfileRepository).save(argThat(profile ->
                profile.getUserAccount() == user && profile.getJumpCm() == 40));
    }

    @Test
    void shouldThrowWhenProfileAlreadyExists() {
        UserAccount user = new UserAccount();
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(personProfileRepository.findByUserAccount(user)).thenReturn(Optional.of(new PersonProfile()));

        assertThrows(PersonProfileAlreadyExistsException.class, () ->
                personProfileService.createProfile(
                        new PersonProfileCreateRequest("Jan", "Kowalski", null, null), EMAIL));

        verify(personProfileRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProfileDoesNotExist() {
        UUID externalId = UUID.randomUUID();
        when(personProfileRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

        assertThrows(PersonProfileNotFoundException.class,
                () -> personProfileService.getProfileByExternalId(externalId));
    }

    @Test
    void shouldGetMyProfile() {
        UserAccount user = new UserAccount();
        PersonProfile profile = PersonProfile.builder().firstName("Jan").lastName("Kowalski").build();
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(personProfileRepository.findByUserAccount(user)).thenReturn(Optional.of(profile));

        assertThat(personProfileService.getMyProfile(EMAIL).firstName()).isEqualTo("Jan");
    }

    @Test
    void shouldUpdateProfile() {
        UserAccount user = new UserAccount();
        PersonProfile profile = PersonProfile.builder().firstName("Jan").lastName("Kowalski").build();
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(personProfileRepository.findByUserAccount(user)).thenReturn(Optional.of(profile));

        var result = personProfileService.updateProfile(
                new PersonProfileUpdateRequest(null, null, "Jasiek", 45), EMAIL);

        assertThat(result.displayName()).isEqualTo("Jasiek");
        assertThat(profile.getJumpCm()).isEqualTo(45);
    }

    @Test
    void shouldNotDeleteProfileWithMemberships() {
        UserAccount user = new UserAccount();
        PersonProfile profile = PersonProfile.builder().id(3L).build();
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(personProfileRepository.findByUserAccount(user)).thenReturn(Optional.of(profile));
        when(clubMembershipRepository.existsByPersonProfileId(3L)).thenReturn(true);

        assertThrows(PersonProfileInUseException.class, () -> personProfileService.deleteProfile(EMAIL));

        verify(personProfileRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteProfileWithoutMemberships() {
        UserAccount user = new UserAccount();
        PersonProfile profile = PersonProfile.builder().id(3L).build();
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(personProfileRepository.findByUserAccount(user)).thenReturn(Optional.of(profile));

        personProfileService.deleteProfile(EMAIL);

        verify(personProfileRepository).delete(profile);
    }
}
