package pl.volleyflow.match.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.match.model.MatchCreateRequest;
import pl.volleyflow.match.model.MatchDto;
import pl.volleyflow.match.service.MatchService;

import java.security.Principal;

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


}
