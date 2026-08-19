package pl.volleyflow.match.service;

import pl.volleyflow.match.model.MatchCreateRequest;
import pl.volleyflow.match.model.MatchDto;

public interface MatchService {

    MatchDto createMatch(MatchCreateRequest request, String email);

}
