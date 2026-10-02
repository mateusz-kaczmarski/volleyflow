package pl.volleyflow.match.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.personprofile.model.PersonProfile;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "volleyball_match_set_statistic", uniqueConstraints = @UniqueConstraint(
        name = "uk_match_set_statistic_set_club_player",
        columnNames = {"set_id", "club_id", "player_profile_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetStatisticEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private UUID externalId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private SetEntity set;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "player_profile_id", nullable = false)
    private PersonProfile player;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private int balance;

    @Column(name = "serve_total", nullable = false)
    private int serveTotal;

    @Column(name = "serve_error", nullable = false)
    private int serveError;

    @Column(name = "serve_aces", nullable = false)
    private int serveAces;

    @Column(name = "reception_total", nullable = false)
    private int receptionTotal;

    @Column(name = "reception_error", nullable = false)
    private int receptionError;

    @Column(name = "reception_positive_percent", nullable = false)
    private int receptionPositivePercent;

    @Column(name = "reception_perfect_percent", nullable = false)
    private int receptionPerfectPercent;

    @Column(name = "attack_total", nullable = false)
    private int attackTotal;

    @Column(name = "attack_error", nullable = false)
    private int attackError;

    @Column(name = "attack_blocked", nullable = false)
    private int attackBlocked;

    @Column(name = "attack_points", nullable = false)
    private int attackPoints;

    @Column(name = "block_points", nullable = false)
    private int blockPoints;

    @Column(name = "block_touches", nullable = false)
    private int blockTouches;

    @Column(nullable = false)
    private int defense;

    @Column(nullable = false)
    private int assists;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private int version;

    @PrePersist
    void prePersist() {
        externalId = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
