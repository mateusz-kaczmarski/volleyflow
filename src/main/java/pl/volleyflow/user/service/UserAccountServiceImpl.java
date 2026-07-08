package pl.volleyflow.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;
import pl.volleyflow.common.StringNormalizer;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.model.*;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.util.Optional;

@Service("userAccountService")
@RequiredArgsConstructor
@Log4j2
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserAccountDto create(UserAccountRequest userAccountRequest) {
        String normalizedEmail = StringNormalizer.normalizeEmail(userAccountRequest.email());
        String normalizedPhone = StringNormalizer.trimToNull(userAccountRequest.phone());
        log.info("Creating user account for email {}", normalizedEmail);

        validatePhoneAndEmail(normalizedEmail, normalizedPhone);

        UserAccount userAccount = UserMapper.mapToEntity(new UserAccountRequest(
                normalizedEmail,
                userAccountRequest.password(),
                normalizedPhone
        ));
        userAccount.setPasswordHash(passwordEncoder.encode(userAccountRequest.password()));

        UserAccount savedUser = userAccountRepository.save(userAccount);
        UserAccountDto userAccountDto = UserMapper.mapToDto(savedUser);

        log.info("Created user account: email={}, externalId={}", userAccountDto.email(), userAccountDto.externalId());
        return userAccountDto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccountDto getBasicInfoByEmail(String userEmail) {
        String normalizedEmail = StringNormalizer.normalizeEmail(userEmail);

        UserAccount userAccount = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Basic user info lookup failed: email {} not found", normalizedEmail);
                    return new UserNotFoundException("User not found");
                });
        return UserMapper.mapToDto(userAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findByEmail(String userEmail) {
        return userAccountRepository.findByEmail(StringNormalizer.normalizeEmail(userEmail));
    }

    @Override
    @Transactional
    public void changePassword(UserChangePasswordRequest userChangePasswordRequest, String userEmail) {
        String normalizedEmail = StringNormalizer.normalizeEmail(userEmail);

        UserAccount userAccount = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.oldPassword(), userAccount.getPasswordHash())) {
            userAccount.setPasswordHash(passwordEncoder.encode(userChangePasswordRequest.newPassword()));
            log.info("Successful changed password for user {}", userEmail);
            return;
        }
        log.warn("Password change rejected for user {}: current password is incorrect", normalizedEmail);
        throw new InvalidCredentialsException("Invalid credentials");
    }

    @Override
    @Transactional
    public UserAccountDto updateUserAccount(UserAccountUpdateRequest userAccountUpdateRequest, String userEmail) {
        String normalizedEmail = StringNormalizer.normalizeEmail(userEmail);
        String normalizedUpdatedEmail = StringNormalizer.normalizeOptionalEmail(userAccountUpdateRequest.email());
        String normalizedPhone = userAccountUpdateRequest.phone() == null
                ? null
                : StringNormalizer.trimToNull(userAccountUpdateRequest.phone());

        UserAccount userAccount = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        validateUpdatedEmailAndPhone(userAccount, normalizedUpdatedEmail, normalizedPhone, userAccountUpdateRequest.phone());

        UserMapper.updateEntity(
                userAccount,
                userAccountUpdateRequest,
                normalizedUpdatedEmail,
                normalizedPhone);

        return UserMapper.mapToDto(userAccount);
    }

    @Override
    @Transactional
    public void deleteUserAccount(String userEmail) {
        String normalizedEmail = StringNormalizer.normalizeEmail(userEmail);
        UserAccount userAccount = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userAccount.setStatus(UserAccountStatus.DELETED);
        log.info("Deleted user account: email={}, externalId={}", normalizedEmail, userAccount.getExternalId());
    }

    private void validatePhoneAndEmail(String normalizedEmail, String phone) {
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            log.warn("User account creation rejected: email {} already exists", normalizedEmail);
            throw new UserAccountAlreadyExists("User with email " + normalizedEmail + " already exists");
        }

        if (phone != null && userAccountRepository.existsByPhone(phone)) {
            log.warn("User account creation rejected: phone {} already exists", phone);
            throw new UserAccountAlreadyExists("User with phone " + phone + " already exists");
        }
    }

    private void validateUpdatedEmailAndPhone(UserAccount userAccount,
                                              String normalizedUpdatedEmail,
                                              String normalizedPhone,
                                              String requestedPhone) {
        if (normalizedUpdatedEmail != null
                && userAccountRepository.existsByEmailAndIdNot(normalizedUpdatedEmail, userAccount.getId())) {
            log.warn("User account update rejected: email {} already exists", normalizedUpdatedEmail);
            throw new UserAccountAlreadyExists("User with email " + normalizedUpdatedEmail + " already exists");
        }

        if (requestedPhone != null
                && normalizedPhone != null
                && userAccountRepository.existsByPhoneAndIdNot(normalizedPhone, userAccount.getId())) {
            log.warn("User account update rejected: phone {} already exists", normalizedPhone);
            throw new UserAccountAlreadyExists("User with phone " + normalizedPhone + " already exists");
        }
    }

}
