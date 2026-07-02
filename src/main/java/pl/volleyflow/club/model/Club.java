package pl.volleyflow.club.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "club")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36, updatable = false)
    private UUID externalId;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 255)
    private String avatar;

    @Column(length = 1024)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "club_status", nullable = false, length = 20)
    private ClubStatus clubStatus;

    @Version
    private int version;

    @PrePersist
    void prePersist() {
        externalId = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (clubStatus == null) {
            clubStatus = ClubStatus.ACTIVE;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

}
