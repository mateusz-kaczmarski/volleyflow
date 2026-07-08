package pl.volleyflow.user.service;

import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserChangePasswordRequest;

import java.util.Optional;

public interface UserAccountService {

    UserAccountDto create(UserAccountRequest userAccountRequest);

    UserAccountDto getBasicInfoByEmail(String email);

    Optional<UserAccount> findByEmail(String email);

    void changePassword(UserChangePasswordRequest userChangePasswordRequest, String email);

}
