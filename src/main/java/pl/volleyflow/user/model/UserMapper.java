package pl.volleyflow.user.model;

import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

public class UserMapper {

    public static UserAccount mapToEntity(UserAccountRequest userAccountRequest) {
        return UserAccount.builder()
                .email(userAccountRequest.email())
                .emailVerified(false)
                .lastLoginAt(null)
                .status(UserAccountStatus.PENDING)
                .phone(userAccountRequest.phone())
                .build();
    }

    public static UserAccountDto mapToDto(UserAccount userAccount) {
        return UserAccountDto.builder()
                .externalId(userAccount.getExternalId())
                .email(userAccount.getEmail())
                .phone(userAccount.getPhone())
                .build();
    }

    public static void updateEntity(UserAccount userAccount,
                                    UserAccountUpdateRequest request,
                                    String normalizedEmail,
                                    String normalizedPhone) {
        if (request.email() != null) {
            userAccount.setEmail(normalizedEmail);
        }
        if (request.phone() != null) {
            userAccount.setPhone(normalizedPhone);
        }
    }

}
