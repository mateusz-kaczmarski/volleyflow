package pl.volleyflow.user.service;

import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserAccountUpdateRequest;
import pl.volleyflow.user.model.UserChangePasswordRequest;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountService {

    UserAccountDto create(UserAccountRequest userAccountRequest);

    UserAccountDto getBasicInfoByEmail(String userEmail);

    Optional<UserAccount> findByEmail(String userEmail);

    Optional<UserAccount> findByExternalId(UUID externalId);

    void changePassword(UserChangePasswordRequest userChangePasswordRequest, String userEmail);

    UserAccountDto updateUserAccount(UserAccountUpdateRequest userAccountUpdateRequest, String userEmail);

    void deleteUserAccount(String userEmail);

}
