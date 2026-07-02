package pl.volleyflow.club.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.club.model.ClubBasicDto;
import pl.volleyflow.club.model.ClubDetailsDto;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.model.ClubUpdateRequest;
import pl.volleyflow.club.service.ClubService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping()
    ResponseEntity<ClubBasicDto> createClub(@RequestBody @Valid ClubRequest clubRequest,
                                             Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createClub(clubRequest, principal.getName()));
    }

    @GetMapping("/my-clubs")
    List<ClubBasicDto> getMyClubs(Principal principal) {
        return clubService.getMyClubs(principal.getName());
    }

    @GetMapping("/{clubExternalId}")
    ResponseEntity<ClubBasicDto> getClub(@PathVariable UUID clubExternalId) {
        return ResponseEntity.ok(clubService.getClub(clubExternalId));
    }

    @GetMapping("/{clubExternalId}/details")
    ResponseEntity<ClubDetailsDto> getClubDetails(@PathVariable UUID clubExternalId,
                                                  Principal principal) {
        return ResponseEntity.ok(clubService.getClubDetails(clubExternalId, principal.getName()));
    }

    @PutMapping("/{clubExternalId}")
    ResponseEntity<ClubBasicDto> updateClub(@PathVariable UUID clubExternalId,
                                            @Valid @RequestBody ClubUpdateRequest clubUpdateRequest,
                                            Principal principal) {
        return ResponseEntity.ok(clubService.updateClub(clubUpdateRequest, clubExternalId, principal.getName()));
    }

    @DeleteMapping("/{clubExternalId}")
    ResponseEntity<Void> deleteClub(@PathVariable UUID clubExternalId,
                                    Principal principal) {
        clubService.deleteClub(clubExternalId, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
