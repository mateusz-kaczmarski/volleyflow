package pl.volleyflow.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.service.UserAccountService;

import java.security.Principal;

@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    @GetMapping("/me")
    public ResponseEntity<UserAccountDto> getBasicInfo(Principal principal) {
        return ResponseEntity.ok(userAccountService.getBasicInfoByEmail(principal.getName()));
    }

}
