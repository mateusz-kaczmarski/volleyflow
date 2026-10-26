package pl.volleyflow.match.service.setstatistics;

import pl.volleyflow.match.model.set.SetStatisticsDto;
import pl.volleyflow.match.model.set.SetStatisticsUpdateRequest;
import pl.volleyflow.match.model.setstatistics.PlayerCareerStatisticsDto;

import java.util.UUID;

public interface SetStatisticsService {

    void saveSetStatistics(UUID matchExternalId,
                           UUID clubExternalId,
                           UUID setExternalId,
                           SetStatisticsUpdateRequest setStatisticsUpdateRequest,
                           String email);

    SetStatisticsDto getSetStatistics(UUID matchExternalId,
                                      UUID clubExternalId,
                                      UUID setExternalId,
                                      String email);

    PlayerCareerStatisticsDto getAllStatisticsForPlayer(UUID clubExternalId,
                                                        UUID playerExternalId,
                                                        String email);

}
