package pl.volleyflow.match.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.match.model.Match.MatchSide;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "volleyball_match_team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private UUID externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private MatchSide side;

    @Column(name = "sets_won", nullable = false)
    private int setsWon;

    @Column(name = "created_on")
    private Instant createdOn;

    @PrePersist
    void prePersist() {
        externalId = UUID.randomUUID();
        createdOn = Instant.now();
    }

}
