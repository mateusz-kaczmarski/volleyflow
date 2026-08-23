package pl.volleyflow.match.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.match.model.MatchCreateRequest;
import pl.volleyflow.match.model.MatchDto;
import pl.volleyflow.match.model.MatchUpdateRequest;
import pl.volleyflow.match.service.MatchService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    ResponseEntity<MatchDto> createMatch(@Valid @RequestBody MatchCreateRequest matchCreateRequest,
                                         Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                matchService.createMatch(matchCreateRequest, principal.getName()));
    }

    @PutMapping("/{matchExternalId}")
    ResponseEntity<MatchDto> updateMatch(@PathVariable UUID matchExternalId,
                                         @Valid @RequestBody MatchUpdateRequest matchUpdateRequest,
                                         Principal principal) {
        return ResponseEntity.ok(matchService.updateMatch(matchExternalId, matchUpdateRequest, principal.getName()));
    }

    @GetMapping
    ResponseEntity<List<MatchDto>> getClubMatches(@RequestParam UUID clubExternalId,
                                                  Principal principal) {
        return ResponseEntity.ok(matchService.getClubMatches(clubExternalId, principal.getName()));
    }


}
