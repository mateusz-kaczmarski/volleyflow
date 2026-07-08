package pl.volleyflow.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserChangePasswordRequest;
import pl.volleyflow.user.service.UserAccountService;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    @GetMapping("/me")
    public ResponseEntity<UserAccountDto> getBasicInfo(Principal principal) {
        return ResponseEntity.ok(userAccountService.getBasicInfoByEmail(principal.getName()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid UserChangePasswordRequest userChangePasswordRequest,
                                                Principal principal) {
        userAccountService.changePassword(userChangePasswordRequest, principal.getName());
        return ResponseEntity.ok().build();
    }


}
