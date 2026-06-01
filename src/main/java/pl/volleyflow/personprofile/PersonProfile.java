package pl.volleyflow.personprofile;

import jakarta.persistence.*;
import lombok.*;
import pl.volleyflow.user.entity.UserAccount;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "person_profile", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36, updatable = false)
    private UUID externalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", unique = true)
    private UserAccount userAccount;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(name = "jump_cm")
    private Integer jumpCm;

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
