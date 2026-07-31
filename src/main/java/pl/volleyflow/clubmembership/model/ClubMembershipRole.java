package pl.volleyflow.clubmembership.model;

import java.util.List;

public enum ClubMembershipRole {

    PLAYER,
    OWNER,
    TRAINER,
    STATISTIC;

    public static List<ClubMembershipRole> getAllRoles() {
        return List.of(PLAYER, OWNER, TRAINER, STATISTIC);
    }

    public static List<ClubMembershipRole> getStaffRoles() {
        return List.of(OWNER, TRAINER, STATISTIC);
    }

}
