package pl.volleyflow.clubmembership.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.model.ClubMembershipRequest;
import pl.volleyflow.clubmembership.model.ClubMembershipUpdateRequest;
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
    ResponseEntity<ClubMembershipDto> createMembership(@RequestBody @Valid ClubMembershipRequest request,
                                                       Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                clubMembershipService.createMembership(request, principal.getName()));
    }

    @GetMapping("/{clubExternalId}/players")
    List<ClubMembershipDto> getPlayersByClub(@PathVariable UUID clubExternalId,
                                             @RequestParam(required = false) Boolean active,
                                             Principal principal) {
        return clubMembershipService.getPlayersByClub(clubExternalId, principal.getName(), active);
    }

    @GetMapping("/{clubExternalId}/{membershipExternalId}")
    ClubMembershipDto getMembershipDetails(@PathVariable UUID clubExternalId,
                                            @PathVariable UUID membershipExternalId,
                                            Principal principal) {
        return clubMembershipService.getClubMembershipDetails(clubExternalId, membershipExternalId, principal.getName());
    }

    @PutMapping("/{clubExternalId}/{membershipExternalId}")
    ClubMembershipDto updateMembership(@PathVariable UUID clubExternalId,
                                       @PathVariable UUID membershipExternalId,
                                       @RequestBody @Valid ClubMembershipUpdateRequest request,
                                       Principal principal) {
        return clubMembershipService.updateMembership(clubExternalId, membershipExternalId, request, principal.getName());
    }

    @DeleteMapping("/{clubExternalId}/{membershipExternalId}")
    ResponseEntity<Void> deleteMembership(@PathVariable UUID clubExternalId,
                                          @PathVariable UUID membershipExternalId,
                                          Principal principal) {
        clubMembershipService.deleteMembership(clubExternalId, membershipExternalId, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
