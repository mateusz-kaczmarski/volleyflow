package pl.volleyflow.match.service;

import pl.volleyflow.match.model.MatchCreateRequest;
import pl.volleyflow.match.model.MatchDto;
import pl.volleyflow.match.model.MatchUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface MatchService {

    MatchDto createMatch(MatchCreateRequest request, String email);

    MatchDto updateMatch(UUID matchExternalId, MatchUpdateRequest request, String email);

    List<MatchDto> getClubMatches(UUID clubExternalId, String email);

    MatchDto getMatch(UUID matchExternalId, String email);

}
