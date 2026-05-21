package pl.volleyflow.club.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.club.model.ClubDto;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.service.ClubService;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/internal/club")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping()
    ResponseEntity<ClubDto> createClub(@RequestBody @Valid ClubRequest clubRequest) {
            return ResponseEntity.ok(clubService.createClub(clubRequest));
    }

}
