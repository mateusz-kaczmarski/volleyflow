package pl.volleyflow.clubmembership.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ClubMembershipRepositoryTest {

    @Autowired
    private ClubMembershipRepository membershipRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindActiveMembershipAndRole() {
        Fixture fixture = savePlayer();

        assertThat(membershipRepository.findActiveMembership(fixture.club().getExternalId(),
                fixture.membership().getExternalId(), ClubStatus.ACTIVE)).contains(fixture.membership());
        assertThat(membershipRepository.findRole(fixture.club().getExternalId(),
                fixture.user().getEmail(), ClubStatus.ACTIVE)).contains(ClubMembershipRole.PLAYER);
        assertThat(membershipRepository.isClubMember(fixture.club().getExternalId(),
                fixture.user().getEmail(), ClubStatus.ACTIVE)).isTrue();
    }

    @Test
    void shouldFindPlayersAndFilterByActiveStatus() {
        Fixture fixture = savePlayer();

        assertThat(membershipRepository.findByClubAndRoles(fixture.club().getExternalId(),
                List.of(ClubMembershipRole.PLAYER), true, ClubStatus.ACTIVE))
                .extracting(ClubMembership::getExternalId)
                .containsExactly(fixture.membership().getExternalId());
        assertThat(membershipRepository.findByClubAndRoles(fixture.club().getExternalId(),
                List.of(ClubMembershipRole.PLAYER), false, ClubStatus.ACTIVE)).isEmpty();
    }

    @Test
    void shouldDetectDuplicatePlayerAndShirtNumber() {
        Fixture fixture = savePlayer();

        assertThat(membershipRepository.existsPlayer(fixture.club().getExternalId(),
                "Jan", "Kowalski", ClubStatus.ACTIVE)).isTrue();
        assertThat(membershipRepository.existsShirtNumber(fixture.club().getExternalId(),
                10, ClubStatus.ACTIVE)).isTrue();
        assertThat(membershipRepository.existsPlayerExcept(fixture.club().getExternalId(),
                "Jan", "Kowalski", fixture.membership().getExternalId(), ClubStatus.ACTIVE)).isFalse();
    }

    @Test
    void shouldRecognizeActivePlayerAndOwnerRole() {
        Fixture fixture = savePlayer();

        assertThat(membershipRepository.hasActivePlayer(fixture.club().getId(),
                fixture.profile().getExternalId())).isTrue();
        assertThat(membershipRepository.hasActiveMembership(fixture.user().getId(),
                fixture.club().getId())).isTrue();
        assertThat(membershipRepository.hasRole(fixture.user().getId(),
                fixture.club().getId(), ClubMembershipRole.OWNER)).isFalse();
    }

    @Test
    void shouldDeactivateMembership() {
        Fixture fixture = savePlayer();

        int updated = membershipRepository.deactivateMembership(fixture.club().getExternalId(),
                fixture.membership().getExternalId(), ClubStatus.ACTIVE, Instant.now());
        entityManager.clear();

        assertThat(updated).isEqualTo(1);
        assertThat(membershipRepository.findActiveMembership(fixture.club().getExternalId(),
                fixture.membership().getExternalId(), ClubStatus.ACTIVE)).isEmpty();
        assertThat(membershipRepository.hasActivePlayer(fixture.club().getId(),
                fixture.profile().getExternalId())).isFalse();
    }

    private Fixture savePlayer() {
        Club club = entityManager.persistAndFlush(Club.builder()
                .name("Test club").clubStatus(ClubStatus.ACTIVE).build());
        UserAccount user = entityManager.persistAndFlush(UserAccount.builder()
                .email("player@example.com").passwordHash("hash")
                .status(UserAccountStatus.ACTIVE).build());
        PersonProfile profile = entityManager.persistAndFlush(PersonProfile.builder()
                .userAccount(user).firstName("Jan").lastName("Kowalski").build());
        ClubMembership membership = entityManager.persistAndFlush(ClubMembership.builder()
                .club(club).personProfile(profile).role(ClubMembershipRole.PLAYER)
                .shirtNumber(10).build());
        return new Fixture(club, user, profile, membership);
    }

    private record Fixture(Club club, UserAccount user, PersonProfile profile, ClubMembership membership) {
    }
}
