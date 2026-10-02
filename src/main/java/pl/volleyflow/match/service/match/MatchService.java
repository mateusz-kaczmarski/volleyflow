package pl.volleyflow.match.service.match;

import pl.volleyflow.match.model.Match.MatchCreateRequest;
import pl.volleyflow.match.model.Match.MatchDto;
import pl.volleyflow.match.model.Match.MatchUpdateRequest;
import pl.volleyflow.match.model.Set.*;

import java.util.List;
import java.util.UUID;

public interface MatchService {

    MatchDto createMatch(MatchCreateRequest request, String email);

    MatchDto updateMatch(UUID matchExternalId, MatchUpdateRequest request, String email);

    List<MatchDto> getClubMatches(UUID clubExternalId, String email);

    MatchDto getMatchDetails(UUID matchExternalId, UUID clubExternalId, String email);

    void finishMatch(UUID matchExternalId, String email);

}
