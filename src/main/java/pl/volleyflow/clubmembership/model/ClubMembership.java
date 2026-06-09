package pl.volleyflow.clubmembership.model;

import jakarta.persistence.*;
import lombok.*;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.personprofile.model.PersonProfile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "club_membership", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36, updatable = false)
    private UUID externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_profile_id", nullable = false)
    private PersonProfile personProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClubMembershipRole role;

    @Column(name = "shirt_number")
    private Integer shirtNumber;

    @Column(length = 20)
    private String season;

    @Builder.Default
    @ElementCollection(targetClass = MemberPosition.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "club_membership_position",
            schema = "app",
            joinColumns = @JoinColumn(name = "club_membership_id")
    )
    @Column(name = "position", nullable = false, length = 30)
    private Set<MemberPosition> positions = new HashSet<>();

    @Column(nullable = false)
    private boolean active;

    @Column(name = "active_from")
    private LocalDate activeFrom;

    @Column(name = "active_to")
    private LocalDate activeTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private int version;

    @PrePersist
    void createClubMemberShip() {
        externalId = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void updateClubMemberShip() {
        updatedAt = Instant.now();
    }

}
