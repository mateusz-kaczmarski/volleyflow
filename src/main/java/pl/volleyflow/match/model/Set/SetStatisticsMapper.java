package pl.volleyflow.match.model.Set;

import pl.volleyflow.club.model.Club;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.entity.SetStatisticEntity;
import pl.volleyflow.personprofile.model.PersonProfile;

import java.util.List;

public final class SetStatisticsMapper {

    private SetStatisticsMapper() {
    }

    public static SetStatisticsDto mapToDto(SetStatisticsCreateRequest request) {
        return mapPlayersToDto(request.players());
    }

    public static SetStatisticsDto mapToDto(SetStatisticsUpdateRequest request) {
        return mapPlayersToDto(request.players());
    }

    private static SetStatisticsDto mapPlayersToDto(List<SetStatisticInput> players) {
        return new SetStatisticsDto(players.stream().map(SetStatisticsMapper::mapToDto).toList());
    }

    public static SetStatisticDto mapToDto(SetStatisticInput player) {
        return new SetStatisticDto(
                player.playerExternalId(),
                player.points(),
                player.balance(),
                player.serveTotal(),
                player.serveError(),
                player.serveAces(),
                player.receptionTotal(),
                player.receptionError(),
                player.receptionPositivePercent(),
                player.receptionPerfectPercent(),
                player.attackTotal(),
                player.attackError(),
                player.attackBlocked(),
                player.attackPoints(),
                player.blockPoints(),
                player.blockTouches(),
                player.defense(),
                player.assists());
    }

    public static SetStatisticDto mapToDto(SetStatisticEntity statistic) {
        return new SetStatisticDto(
                statistic.getPlayer().getExternalId(),
                statistic.getPoints(),
                statistic.getBalance(),
                statistic.getServeTotal(),
                statistic.getServeError(),
                statistic.getServeAces(),
                statistic.getReceptionTotal(),
                statistic.getReceptionError(),
                statistic.getReceptionPositivePercent(),
                statistic.getReceptionPerfectPercent(),
                statistic.getAttackTotal(),
                statistic.getAttackError(),
                statistic.getAttackBlocked(),
                statistic.getAttackPoints(),
                statistic.getBlockPoints(),
                statistic.getBlockTouches(),
                statistic.getDefense(),
                statistic.getAssists());
    }

    public static SetStatisticEntity mapToEntity(SetStatisticInput input,
                                                 SetEntity set,
                                                 Club club,
                                                 PersonProfile player) {
        return SetStatisticEntity.builder()
                .set(set)
                .club(club)
                .player(player)
                .points(input.points())
                .balance(input.balance())
                .serveTotal(input.serveTotal())
                .serveError(input.serveError())
                .serveAces(input.serveAces())
                .receptionTotal(input.receptionTotal())
                .receptionError(input.receptionError())
                .receptionPositivePercent(input.receptionPositivePercent())
                .receptionPerfectPercent(input.receptionPerfectPercent())
                .attackTotal(input.attackTotal())
                .attackError(input.attackError())
                .attackBlocked(input.attackBlocked())
                .attackPoints(input.attackPoints())
                .blockPoints(input.blockPoints())
                .blockTouches(input.blockTouches())
                .defense(input.defense())
                .assists(input.assists())
                .build();
    }

    public static void updateEntity(SetStatisticEntity statistic, SetStatisticInput input) {
        statistic.setPoints(input.points());
        statistic.setBalance(input.balance());
        statistic.setServeTotal(input.serveTotal());
        statistic.setServeError(input.serveError());
        statistic.setServeAces(input.serveAces());
        statistic.setReceptionTotal(input.receptionTotal());
        statistic.setReceptionError(input.receptionError());
        statistic.setReceptionPositivePercent(input.receptionPositivePercent());
        statistic.setReceptionPerfectPercent(input.receptionPerfectPercent());
        statistic.setAttackTotal(input.attackTotal());
        statistic.setAttackError(input.attackError());
        statistic.setAttackBlocked(input.attackBlocked());
        statistic.setAttackPoints(input.attackPoints());
        statistic.setBlockPoints(input.blockPoints());
        statistic.setBlockTouches(input.blockTouches());
        statistic.setDefense(input.defense());
        statistic.setAssists(input.assists());
    }
}
