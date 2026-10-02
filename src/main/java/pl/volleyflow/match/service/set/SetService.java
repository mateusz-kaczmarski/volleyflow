package pl.volleyflow.match.service.set;

import pl.volleyflow.match.model.Set.SetCreateRequest;
import pl.volleyflow.match.model.Set.SetDto;
import pl.volleyflow.match.model.Set.SetUpdateRequest;

import java.util.UUID;

public interface SetService {

    SetDto createSet(UUID matchExternalId,
                     UUID clubExternalId,
                     SetCreateRequest setCreateRequest,
                     String email);

    SetDto updateSet(UUID matchExternalId,
                     UUID clubExternalId,
                     UUID setExternalId,
                     SetUpdateRequest setUpdateRequest,
                     String email);

    SetDto updateSetVideo(UUID matchExternalId,
                          UUID clubExternalId,
                          UUID setExternalId,
                          String videoUrl,
                          String email);

    void deleteSet(UUID matchExternalId,
                   UUID clubExternalId,
                   UUID setExternalId,
                   String email);


}
