package pl.volleyflow.match.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.MatchAddress;
import pl.volleyflow.match.model.match.MatchStatus;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindMatchByExternalId() {
        Club club = saveClub();
        UserAccount user = saveUser();
        Match match = saveMatch(club, user, 2026, Instant.parse("2026-06-01T12:00:00Z"));

        assertThat(matchRepository.findByExternalId(match.getExternalId()))
                .isPresent()
                .get()
                .extracting(Match::getId)
                .isEqualTo(match.getId());
    }

    @Test
    void shouldFilterAndSortMatchesBySeason() {
        Club club = saveClub();
        UserAccount user = saveUser();
        Match later = saveMatch(club, user, 2026, Instant.parse("2026-07-01T12:00:00Z"));
        Match earlier = saveMatch(club, user, 2026, Instant.parse("2026-06-01T12:00:00Z"));
        saveMatch(club, user, 2025, Instant.parse("2025-06-01T12:00:00Z"));

        assertThat(matchRepository.findByCreatedByClubExternalIdAndSeasonNumberOrderByScheduledAtAsc(
                club.getExternalId(), 2026))
                .extracting(Match::getExternalId)
                .containsExactly(earlier.getExternalId(), later.getExternalId());
    }

    @Test
    void shouldListDistinctSeasonsDescending() {
        Club club = saveClub();
        UserAccount user = saveUser();
        saveMatch(club, user, 2025, Instant.parse("2025-06-01T12:00:00Z"));
        saveMatch(club, user, 2026, Instant.parse("2026-06-01T12:00:00Z"));
        saveMatch(club, user, 2026, Instant.parse("2026-07-01T12:00:00Z"));

        assertThat(matchRepository.findDistinctSeasonNumbersByCreatedByClub(club))
                .containsExactly(2026, 2025);
    }

    private Club saveClub() {
        return entityManager.persistAndFlush(Club.builder()
                .name("Test club")
                .clubStatus(ClubStatus.ACTIVE)
                .build());
    }

    private UserAccount saveUser() {
        return entityManager.persistAndFlush(UserAccount.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .status(UserAccountStatus.ACTIVE)
                .build());
    }

    private Match saveMatch(Club club, UserAccount user, int season, Instant scheduledAt) {
        return entityManager.persistAndFlush(Match.builder()
                .createdByClub(club)
                .createdBy(user)
                .status(MatchStatus.SCHEDULED)
                .scheduledAt(scheduledAt)
                .seasonNumber(season)
                .location(MatchAddress.builder().city("Warszawa").zipCode("00-001")
                        .street("Sportowa").buildingNumber("1").build())
                .build());
    }
}
