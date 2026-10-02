package pl.volleyflow.match.model.Set;

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
        int receptionTotal,
        int receptionError,
        int receptionPositivePercent,
        int receptionPerfectPercent,
        int attackTotal,
        int attackError,
        int attackBlocked,
        int attackPoints,
        int blockPoints,
        int blockTouches,
        int defense,
        int assists) {
}
