package pl.volleyflow.match.service.setstatistics;

import pl.volleyflow.match.model.Set.SetStatisticsDto;
import pl.volleyflow.match.model.Set.SetStatisticsUpdateRequest;

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

}
