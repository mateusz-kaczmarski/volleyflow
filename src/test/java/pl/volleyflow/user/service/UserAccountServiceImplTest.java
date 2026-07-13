package pl.volleyflow.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.model.UserAccountAlreadyExists;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserAccountUpdateRequest;
import pl.volleyflow.user.model.UserChangePasswordRequest;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final UUID EXTERNAL_ID = UUID.fromString("11b8560c-ed43-4122-b6db-46e7698b1b2f");
    private static final String EMAIL = "email@example.pl";
    private static final String UPDATED_EMAIL = "updated@example.pl";
    private static final String PHONE = "123456789";
    private static final String UPDATED_PHONE = "987654321";
    private static final String PASSWORD = "password";
    private static final String OLD_PASSWORD = "old-password";
    private static final String NEW_PASSWORD = "new-password";
    private static final String PASSWORD_HASH = "encoded-password";
    private static final String NEW_PASSWORD_HASH = "encoded-new-password";

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountServiceImpl userAccountService;

    @Test
    void shouldCreateUserAccountWhenEmailAndPhoneAreAvailable() {
        UserAccountRequest userAccountRequest = createUserAccountRequest();
        UserAccount savedUserAccount = createUserAccount();

        when(userAccountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userAccountRepository.existsByPhone(PHONE)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUserAccount);

        UserAccountDto result = userAccountService.create(userAccountRequest);

        assertThat(result).isEqualTo(createUserAccountDto());

        ArgumentCaptor<UserAccount> userAccountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userAccountCaptor.capture());
        assertThat(userAccountCaptor.getValue())
                .extracting(
                        UserAccount::getEmail,
                        UserAccount::getPhone,
                        UserAccount::getPasswordHash,
                        UserAccount::getStatus,
                        UserAccount::isEmailVerified)
                .containsExactly(EMAIL, PHONE, PASSWORD_HASH, UserAccountStatus.ACTIVE, true);
    }

    @Test
    void shouldNormalizeEmailAndPhoneWhenCreateUserAccount() {
        UserAccountRequest userAccountRequest = UserAccountRequest.builder()
                .email(" EMAIL@EXAMPLE.PL ")
                .password(PASSWORD)
                .phone(" 123456789 ")
                .build();

        when(userAccountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userAccountRepository.existsByPhone(PHONE)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(createUserAccount());

        userAccountService.create(userAccountRequest);

        ArgumentCaptor<UserAccount> userAccountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).existsByEmail(EMAIL);
        verify(userAccountRepository).existsByPhone(PHONE);
        verify(userAccountRepository).save(userAccountCaptor.capture());
        assertThat(userAccountCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(userAccountCaptor.getValue().getPhone()).isEqualTo(PHONE);
    }

    @Test
    void shouldNotCheckPhoneWhenCreateUserAccountWithBlankPhone() {
        UserAccountRequest userAccountRequest = UserAccountRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .phone(" ")
                .build();

        when(userAccountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(createUserAccount(null));

        UserAccountDto result = userAccountService.create(userAccountRequest);

        assertThat(result).isEqualTo(createUserAccountDto(null));
        verify(userAccountRepository, never()).existsByPhone(any());
    }

    @Test
    void shouldThrowUserAccountAlreadyExistsWhenCreateUserAndEmailExists() {
        UserAccountRequest userAccountRequest = createUserAccountRequest();

        when(userAccountRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(UserAccountAlreadyExists.class, () -> userAccountService.create(userAccountRequest));

        verify(userAccountRepository, never()).existsByPhone(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void shouldThrowUserAccountAlreadyExistsWhenCreateUserAndPhoneExists() {
        UserAccountRequest userAccountRequest = createUserAccountRequest();

        when(userAccountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userAccountRepository.existsByPhone(PHONE)).thenReturn(true);

        assertThrows(UserAccountAlreadyExists.class, () -> userAccountService.create(userAccountRequest));

        verify(passwordEncoder, never()).encode(any());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void shouldReturnBasicInfoByEmail() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(createUserAccount()));

        UserAccountDto result = userAccountService.getBasicInfoByEmail(" EMAIL@EXAMPLE.PL ");

        assertThat(result).isEqualTo(createUserAccountDto());
        verify(userAccountRepository).findByEmail(EMAIL);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserIsNotFoundByEmail() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userAccountService.getBasicInfoByEmail(EMAIL));
    }

    @Test
    void shouldFindUserByNormalizedEmail() {
        UserAccount userAccount = createUserAccount();

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));

        Optional<UserAccount> result = userAccountService.findByEmail(" EMAIL@EXAMPLE.PL ");

        assertThat(result).contains(userAccount);
        verify(userAccountRepository).findByEmail(EMAIL);
    }

    @Test
    void shouldChangePasswordWhenOldPasswordMatches() {
        UserAccount userAccount = createUserAccount();
        UserChangePasswordRequest request = createChangePasswordRequest();

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches(OLD_PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_HASH);

        userAccountService.changePassword(request, " EMAIL@EXAMPLE.PL ");

        assertThat(userAccount.getPasswordHash()).isEqualTo(NEW_PASSWORD_HASH);
        verify(userAccountRepository).findByEmail(EMAIL);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenChangingPasswordAndUserIsNotFound() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userAccountService.changePassword(createChangePasswordRequest(), EMAIL));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenOldPasswordDoesNotMatch() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(createUserAccount()));
        when(passwordEncoder.matches(OLD_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userAccountService.changePassword(createChangePasswordRequest(), EMAIL));

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldUpdateUserAccount() {
        UserAccount userAccount = createUserAccount();
        UserAccountUpdateRequest request = new UserAccountUpdateRequest(UPDATED_EMAIL, UPDATED_PHONE);

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));
        when(userAccountRepository.existsByEmailAndIdNot(UPDATED_EMAIL, USER_ID)).thenReturn(false);
        when(userAccountRepository.existsByPhoneAndIdNot(UPDATED_PHONE, USER_ID)).thenReturn(false);

        UserAccountDto result = userAccountService.updateUserAccount(request, EMAIL);

        assertThat(result).isEqualTo(createUserAccountDto(UPDATED_EMAIL, UPDATED_PHONE));
        assertThat(userAccount.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(userAccount.getPhone()).isEqualTo(UPDATED_PHONE);
    }

    @Test
    void shouldNormalizeEmailAndPhoneWhenUpdateUserAccount() {
        UserAccount userAccount = createUserAccount();
        UserAccountUpdateRequest request = new UserAccountUpdateRequest(" UPDATED@EXAMPLE.PL ", " 987654321 ");

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));
        when(userAccountRepository.existsByEmailAndIdNot(UPDATED_EMAIL, USER_ID)).thenReturn(false);
        when(userAccountRepository.existsByPhoneAndIdNot(UPDATED_PHONE, USER_ID)).thenReturn(false);

        UserAccountDto result = userAccountService.updateUserAccount(request, " EMAIL@EXAMPLE.PL ");

        assertThat(result).isEqualTo(createUserAccountDto(UPDATED_EMAIL, UPDATED_PHONE));
        verify(userAccountRepository).findByEmail(EMAIL);
        verify(userAccountRepository).existsByEmailAndIdNot(UPDATED_EMAIL, USER_ID);
        verify(userAccountRepository).existsByPhoneAndIdNot(UPDATED_PHONE, USER_ID);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUpdateUserAccountAndUserIsNotFound() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userAccountService.updateUserAccount(new UserAccountUpdateRequest(UPDATED_EMAIL, UPDATED_PHONE), EMAIL));

        verify(userAccountRepository, never()).existsByEmailAndIdNot(any(), any());
        verify(userAccountRepository, never()).existsByPhoneAndIdNot(any(), any());
    }

    @Test
    void shouldThrowUserAccountAlreadyExistsWhenUpdatedEmailExists() {
        UserAccount userAccount = createUserAccount();

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));
        when(userAccountRepository.existsByEmailAndIdNot(UPDATED_EMAIL, USER_ID)).thenReturn(true);

        assertThrows(UserAccountAlreadyExists.class,
                () -> userAccountService.updateUserAccount(new UserAccountUpdateRequest(UPDATED_EMAIL, UPDATED_PHONE), EMAIL));

        assertThat(userAccount.getEmail()).isEqualTo(EMAIL);
        assertThat(userAccount.getPhone()).isEqualTo(PHONE);
        verify(userAccountRepository, never()).existsByPhoneAndIdNot(any(), any());
    }

    @Test
    void shouldThrowUserAccountAlreadyExistsWhenUpdatedPhoneExists() {
        UserAccount userAccount = createUserAccount();

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));
        when(userAccountRepository.existsByEmailAndIdNot(UPDATED_EMAIL, USER_ID)).thenReturn(false);
        when(userAccountRepository.existsByPhoneAndIdNot(UPDATED_PHONE, USER_ID)).thenReturn(true);

        assertThrows(UserAccountAlreadyExists.class,
                () -> userAccountService.updateUserAccount(new UserAccountUpdateRequest(UPDATED_EMAIL, UPDATED_PHONE), EMAIL));

        assertThat(userAccount.getEmail()).isEqualTo(EMAIL);
        assertThat(userAccount.getPhone()).isEqualTo(PHONE);
    }

    @Test
    void shouldClearPhoneWhenUpdateUserAccountWithBlankPhone() {
        UserAccount userAccount = createUserAccount();
        UserAccountUpdateRequest request = new UserAccountUpdateRequest(null, " ");

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));

        UserAccountDto result = userAccountService.updateUserAccount(request, EMAIL);

        assertThat(result).isEqualTo(createUserAccountDto(EMAIL, null));
        assertThat(userAccount.getPhone()).isNull();
        verify(userAccountRepository, never()).existsByEmailAndIdNot(any(), any());
        verify(userAccountRepository, never()).existsByPhoneAndIdNot(any(), any());
    }

    @Test
    void shouldDeleteUserAccount() {
        UserAccount userAccount = createUserAccount();

        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userAccount));

        userAccountService.deleteUserAccount(" EMAIL@EXAMPLE.PL ");

        assertThat(userAccount.getStatus()).isEqualTo(UserAccountStatus.DELETED);
        verify(userAccountRepository).findByEmail(EMAIL);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenDeleteUserAccountAndUserIsNotFound() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userAccountService.deleteUserAccount(EMAIL));
    }

    private UserAccountRequest createUserAccountRequest() {
        return UserAccountRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .phone(PHONE)
                .build();
    }

    private UserChangePasswordRequest createChangePasswordRequest() {
        return new UserChangePasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    }

    private UserAccount createUserAccount() {
        return createUserAccount(PHONE);
    }

    private UserAccount createUserAccount(String phone) {
        return UserAccount.builder()
                .id(USER_ID)
                .externalId(EXTERNAL_ID)
                .email(EMAIL)
                .passwordHash(PASSWORD_HASH)
                .phone(phone)
                .status(UserAccountStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

    private UserAccountDto createUserAccountDto() {
        return createUserAccountDto(EMAIL, PHONE);
    }

    private UserAccountDto createUserAccountDto(String phone) {
        return createUserAccountDto(EMAIL, phone);
    }

    private UserAccountDto createUserAccountDto(String email, String phone) {
        return UserAccountDto.builder()
                .externalId(EXTERNAL_ID)
                .email(email)
                .phone(phone)
                .build();
    }

}
