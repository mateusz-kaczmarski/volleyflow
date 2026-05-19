package pl.volleyflow.user.service;

import pl.volleyflow.auth.controller.UserLoginRequest;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;

import java.util.Optional;

public interface UserAccountService {

    UserAccountDto createUser(UserAccountRequest userAccountRequest);

    Optional<UserAccount> findByEmail(String email);

}
