package pl.volleyflow.match.model.Set;

import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.SetEntity;

public final class SetMapper {

    private SetMapper() {
    }

    public static SetEntity mapToEntity(SetCreateRequest request, Match match, int setNumber) {
        return SetEntity.builder()
                .match(match)
                .setNumber(setNumber)
                .homePoints(request.homePoints())
                .awayPoints(request.awayPoints())
                .build();
    }

    public static void updateEntity(SetEntity set, SetUpdateRequest request) {
        set.setHomePoints(request.homePoints());
        set.setAwayPoints(request.awayPoints());
    }

    public static SetDto mapToDto(SetEntity set) {
        return new SetDto(
                set.getExternalId(),
                set.getSetNumber(),
                set.getHomePoints(),
                set.getAwayPoints(),
                set.getVideoUrl());
    }

}
