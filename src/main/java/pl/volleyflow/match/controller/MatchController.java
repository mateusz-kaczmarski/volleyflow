package pl.volleyflow.match.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.match.model.match.MatchCreateRequest;
import pl.volleyflow.match.model.match.MatchDto;
import pl.volleyflow.match.model.match.MatchMvpUpdateRequest;
import pl.volleyflow.match.model.match.MatchUpdateRequest;
import pl.volleyflow.match.model.set.*;
import pl.volleyflow.match.model.setstatistics.PlayerCareerStatisticsDto;
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

    @GetMapping()
    ResponseEntity<List<MatchDto>> getClubMatches(@RequestParam UUID clubExternalId,
                                                  @RequestParam(required = false) Integer season,
                                                  Principal principal) {
        return ResponseEntity.ok(matchService.getClubMatches(clubExternalId, principal.getName(), season));
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

    @PutMapping("/{clubExternalId}/{matchExternalId}/mvp")
    ResponseEntity<MatchDto> updateMvp(@PathVariable UUID clubExternalId,
                                       @PathVariable UUID matchExternalId,
                                       @RequestBody MatchMvpUpdateRequest request,
                                       Principal principal) {
        return ResponseEntity.ok(matchService.updateMvp(
                matchExternalId, clubExternalId, request.mvpPlayerExternalId(), principal.getName()));
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

    @PutMapping("/{clubExternalId}/{matchExternalId}/set/{setExternalId}/video")
    ResponseEntity<SetDto> updateSetVideo(@PathVariable UUID clubExternalId,
                                          @PathVariable UUID matchExternalId,
                                          @PathVariable UUID setExternalId,
                                          @Valid @RequestBody SetVideoUpdateRequest request,
                                          Principal principal) {
        return ResponseEntity.ok(setService.updateSetVideo(
                matchExternalId, clubExternalId, setExternalId, request.videoUrl(), principal.getName()));
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

    @GetMapping("/clubs/{clubExternalId}/players/{playerExternalId}/statistics")
    ResponseEntity<PlayerCareerStatisticsDto> getAllStatisticsForPlayer(@PathVariable UUID clubExternalId,
                                                                        @PathVariable UUID playerExternalId,
                                                                        Principal principal) {
        return ResponseEntity.ok().body(setStatisticsService.getAllStatisticsForPlayer(
                clubExternalId,
                playerExternalId,
                principal.getName()));
    }

    @GetMapping("/clubs/{clubExternalId}/getSeasons")
    List<Integer> getSeasonsForClub(@PathVariable UUID clubExternalId,
                                    Principal principal) {
        return matchService.getAllSeasonsForClub(clubExternalId, principal.getName());
    }

}
