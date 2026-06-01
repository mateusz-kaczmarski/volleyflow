package pl.volleyflow.personprofile.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/profile")
public class PersonProfileController {

    private final PersonProfileService personProfileService;

    @PostMapping()
    ResponseEntity<PersonProfileDto> createProfile(@RequestBody @Valid PersonProfileRequest personProfileRequest) {
        return ResponseEntity.ok(personProfileService.createProfile(personProfileRequest));
    }

}
