package pl.volleyflow.match.model.set;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SetStatisticDto(
        UUID playerExternalId,
        int points,
        int balance,
        int serveTotal,
        int serveError,
        int serveAces,
        int opponentReceivedServeCount,
        int receptionTotal,
        int receptionError,
        int receptionNegativeCount,
        int receptionNeutralCount,
        int receptionPositiveCount,
        int receptionPerfectCount,
        int receptionPositivePercent,
        int receptionPerfectPercent,
        int attackTotal,
        int attackError,
        int attackBlocked,
        int attackPoints,
        int opponentDefendedAttackCount,
        int blockPoints,
        int blockTouches,
        int defense,
        int assists) {
}
