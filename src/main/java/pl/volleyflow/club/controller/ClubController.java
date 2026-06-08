package pl.volleyflow.club.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.club.model.ClubDto;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.service.ClubService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/club")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping()
    ResponseEntity<ClubDto> createClub(@RequestBody @Valid ClubRequest clubRequest,
                                       Principal principal) {
            return ResponseEntity.ok(clubService.createClub(clubRequest, principal.getName()));
    }

    @GetMapping("/{userExternalId}")
    List<ClubDto> getClubsByUser(@PathVariable UUID userExternalId) {
        return clubService.getClubsByUser(userExternalId);
    }

}
