package pl.volleyflow.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.common.StringNormalizer;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.*;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.util.Optional;

@Service("userAccountService")
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
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
    public UserAccountDto getBasicInfoByEmail(String email) {
        String normalizedEmail = StringNormalizer.normalizeEmail(email);

        UserAccount userAccount = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Basic user info lookup failed: email {} not found", normalizedEmail);
                    return new UserNotFoundException("User not found");
                });
        return UserMapper.mapToDto(userAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findByEmail(String email) {
        return userAccountRepository.findByEmail(StringNormalizer.normalizeEmail(email));
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

}
