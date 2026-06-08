package pl.volleyflow.clubmembership.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.clubmembership.model.ClubMembershipRequest;
import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.service.ClubMembershipService;

import javax.validation.Valid;

@RestController
@RequestMapping("/internal/membership")
@RequiredArgsConstructor
public class ClubMembershipController {

    private final ClubMembershipService clubMembershipService;


    @PostMapping()
    ResponseEntity<ClubMembershipDto> createMembership(@RequestBody @Valid ClubMembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubMembershipService.createMembership(request));
    }

}
