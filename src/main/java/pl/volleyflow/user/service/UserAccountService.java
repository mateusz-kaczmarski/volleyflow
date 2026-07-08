package pl.volleyflow.user.service;

import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserAccountUpdateRequest;
import pl.volleyflow.user.model.UserChangePasswordRequest;

import java.util.Optional;

public interface UserAccountService {

    UserAccountDto create(UserAccountRequest userAccountRequest);

    UserAccountDto getBasicInfoByEmail(String userEmail);

    Optional<UserAccount> findByEmail(String userEmail);

    void changePassword(UserChangePasswordRequest userChangePasswordRequest, String userEmail);

    UserAccountDto updateUserAccount(UserAccountUpdateRequest userAccountUpdateRequest, String userEmail);

    void deleteUserAccount(String userEmail);

}
