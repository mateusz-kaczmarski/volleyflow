package pl.volleyflow.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.volleyflow.user.entity.UserAccount;
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
    public UserAccountDto create(UserAccountRequest userAccountRequest) {
        if (userAccountRepository.existsByEmail(userAccountRequest.email())) {
            throw new UserAccountAlreadyExists("User with email " + userAccountRequest.email() + "already exists");
        }

        UserAccount userAccount = UserMapper.mapToEntity(userAccountRequest);
        userAccount.setPasswordHash(passwordEncoder.encode(userAccountRequest.password()));

        UserAccount savedUser = userAccountRepository.save(userAccount);
        UserAccountDto userAccountDto = UserMapper.mapToDto(savedUser);

        log.info("Add user wit e-mail and externalId {}", userAccountDto);
        return userAccountDto;
    }

    @Override
    public UserAccountDto getBasicInfoByEmail(String email) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserMapper.mapToDto(userAccount);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return userAccountRepository.findByEmail(email);
    }


}
