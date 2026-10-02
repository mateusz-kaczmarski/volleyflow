package pl.volleyflow.match.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.match.model.Match.MatchCreateRequest;
import pl.volleyflow.match.model.Match.MatchDto;
import pl.volleyflow.match.model.Match.MatchUpdateRequest;
import pl.volleyflow.match.model.Set.*;
import pl.volleyflow.match.service.match.MatchService;
import pl.volleyflow.match.service.set.SetService;
import pl.volleyflow.match.service.setstatistics.SetStatisticsService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final SetService setService;
    private final SetStatisticsService setStatisticsService;

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

    @GetMapping("/{clubExternalId}/{matchExternalId}")
    ResponseEntity<MatchDto> getMatchDetails(@PathVariable UUID matchExternalId,
                                             @PathVariable UUID clubExternalId,
                                             Principal principal) {
        return ResponseEntity.ok().body(matchService.getMatchDetails(
                matchExternalId,
                clubExternalId,
                principal.getName()));
    }

    @PostMapping("{clubExternalId}/{matchExternalId}/set")
    ResponseEntity<SetDto> createSet(@PathVariable UUID clubExternalId,
                                     @PathVariable UUID matchExternalId,
                                     @Valid @RequestBody SetCreateRequest setCreateRequest,
                                     Principal principal) {
        return ResponseEntity.ok().body(setService.createSet(
                matchExternalId,
                clubExternalId,
                setCreateRequest,
                principal.getName()));
    }

    @PutMapping("/{clubExternalId}/{matchExternalId}/set/{setExternalId}")
    ResponseEntity<SetDto> updateSet(@PathVariable UUID clubExternalId,
                                     @PathVariable UUID matchExternalId,
                                     @PathVariable UUID setExternalId,
                                     @Valid @RequestBody SetUpdateRequest request,
                                     Principal principal) {
        return ResponseEntity.ok(setService.updateSet(
                matchExternalId, clubExternalId, setExternalId, request, principal.getName()));
    }

    @PostMapping("/{matchExternalId}/finish")
    ResponseEntity<Void> finishMatch(@PathVariable UUID matchExternalId,
                                     Principal principal) {
        matchService.finishMatch(matchExternalId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{clubExternalId}/{matchExternalId}/set/{setExternalId}/statistics")
    ResponseEntity<SetStatisticsDto> getSetStatistics(@PathVariable UUID clubExternalId,
                                                      @PathVariable UUID matchExternalId,
                                                      @PathVariable UUID setExternalId,
                                                      Principal principal) {
        return ResponseEntity.ok(setStatisticsService.getSetStatistics(
                matchExternalId, clubExternalId, setExternalId, principal.getName()));
    }

    @PutMapping("/{clubExternalId}/{matchExternalId}/set/{setExternalId}/statistics")
    ResponseEntity<Void> saveSetStatistics(@PathVariable UUID clubExternalId,
                                           @PathVariable UUID matchExternalId,
                                           @PathVariable UUID setExternalId,
                                           @Valid @RequestBody SetStatisticsUpdateRequest setStatisticsUpdateRequest,
                                           Principal principal) {
        setStatisticsService.saveSetStatistics(
                matchExternalId,
                clubExternalId,
                setExternalId,
                setStatisticsUpdateRequest,
                principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{clubExternalId}/{matchExternalId}/set/{setExternalId}")
    ResponseEntity<Void> deleteSet(@PathVariable UUID clubExternalId,
                                   @PathVariable UUID matchExternalId,
                                   @PathVariable UUID setExternalId,
                                   Principal principal) {
        setService.deleteSet(matchExternalId, clubExternalId, setExternalId, principal.getName());
        return ResponseEntity.status(204).build();
    }

}
