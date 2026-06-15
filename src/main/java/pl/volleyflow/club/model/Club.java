package pl.volleyflow.club.model;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "club", schema = "app")
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

    private boolean active;

    @Version
    private int version;

    @PrePersist
    void prePersist() {
        externalId = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = Instant.now();
        active = true;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("externalId", externalId)
                .append("name", name)
                .toString();
    }
}
