package pl.volleyflow.clubmembership.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.clubmembership.model.ClubMembershipRequest;
import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.service.ClubMembershipService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/membership")
@RequiredArgsConstructor
public class ClubMembershipController {

    private final ClubMembershipService clubMembershipService;


    @PostMapping()
    ResponseEntity<ClubMembershipDto> createMembership(@RequestBody @Valid ClubMembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubMembershipService.createMembership(request));
    }

    @GetMapping("/club/{clubExternalId}/players")
    List<ClubMembershipDto> getPlayersByClub(@PathVariable UUID clubExternalId,
                                             Principal principal) {
        return clubMembershipService.getPlayersByClub(clubExternalId, principal.getName());
    }

}
