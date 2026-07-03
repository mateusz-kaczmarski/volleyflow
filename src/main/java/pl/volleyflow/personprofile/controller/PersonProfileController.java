package pl.volleyflow.personprofile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.personprofile.model.PersonProfileCreateRequest;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileUpdateRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class PersonProfileController {

    private final PersonProfileService personProfileService;

    @PostMapping()
    ResponseEntity<PersonProfileDto> createProfile(@RequestBody @Valid PersonProfileCreateRequest personProfileCreateRequest,
                                                   Principal principal) {
        return ResponseEntity.ok(personProfileService.createProfile(personProfileCreateRequest, principal.getName()));
    }

    @PutMapping
    ResponseEntity<PersonProfileDto> updateProfile(@RequestBody @Valid PersonProfileUpdateRequest personProfileUpdateRequest,
                                                   Principal principal) {
        return ResponseEntity.ok(personProfileService.updateProfile(personProfileUpdateRequest, principal.getName()));
    }

    @GetMapping("/{profileExternalId}")
    ResponseEntity<PersonProfileDto> getProfileById(@PathVariable UUID profileExternalId) {
        return ResponseEntity.ok(personProfileService.getProfileByExternalId(profileExternalId));
    }

    @GetMapping("/me")
    ResponseEntity<PersonProfileDto> getMyProfile(Principal principal) {
        return ResponseEntity.ok(personProfileService.getMyProfile(principal.getName()));
    }

}
