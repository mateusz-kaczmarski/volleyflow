package pl.volleyflow.user.model;

import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

public class UserMapper {

    public static UserAccount mapToEntity(UserAccountRequest userAccountRequest) {
        //todo change email and status after add email service
        return UserAccount.builder()
                .email(userAccountRequest.email())
                .emailVerified(true)
                .lastLoginAt(null)
                .status(UserAccountStatus.ACTIVE)
                .phone(userAccountRequest.phone())
                .build();
    }

    public static UserAccountDto mapToDto(UserAccount userAccount) {
        return UserAccountDto.builder()
                .externalId(userAccount.getExternalId())
                .email(userAccount.getEmail())
                .build();
    }

}
