package pl.volleyflow.personprofile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileCreateRequest;
import pl.volleyflow.personprofile.model.PersonProfileMapper;
import pl.volleyflow.personprofile.model.PersonProfileUpdateRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;

import java.security.Principal;

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
        return ResponseEntity.ok(PersonProfileMapper.mapToDto(
                personProfileService.updateProfile(personProfileUpdateRequest, principal.getName())
        ));
    }

}
