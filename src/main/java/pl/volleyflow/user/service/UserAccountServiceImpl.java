package pl.volleyflow.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.*;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.util.Locale;
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
        String normalizedEmail = normalizeEmail(userAccountRequest.email());
        log.info("Creating user account for email {}", normalizedEmail);

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            log.warn("User account creation rejected: email {} already exists", normalizedEmail);
            throw new UserAccountAlreadyExists("User with email " + normalizedEmail + " already exists");
        }

        UserAccount userAccount = UserMapper.mapToEntity(new UserAccountRequest(
                normalizedEmail,
                userAccountRequest.password(),
                userAccountRequest.phone()
        ));
        userAccount.setPasswordHash(passwordEncoder.encode(userAccountRequest.password()));

        UserAccount savedUser = userAccountRepository.save(userAccount);
        UserAccountDto userAccountDto = UserMapper.mapToDto(savedUser);

        log.info("Created user account: email={}, externalId={}", userAccountDto.email(), userAccountDto.externalId());
        return userAccountDto;
    }

    @Override
    public UserAccountDto getBasicInfoByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

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
        return userAccountRepository.findByEmail(normalizeEmail(email));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
