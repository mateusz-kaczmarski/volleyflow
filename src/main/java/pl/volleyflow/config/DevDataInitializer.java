package pl.volleyflow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.club.service.ClubService;
import pl.volleyflow.clubmembership.model.ClubMembershipCreateRequest;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.model.MemberPosition;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.clubmembership.service.ClubMembershipService;
import pl.volleyflow.personprofile.model.PersonProfileRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.service.UserAccountService;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class DevDataInitializer {

    private static final String OWNER_EMAIL = "redcrew@gmail.com";
    private static final String OWNER_PASSWORD = "crecrew";
    private static final String CLUB_NAME = "RedCrew";

    private final UserAccountService userAccountService;
    private final PersonProfileService personProfileService;
    private final ClubService clubService;
    private final ClubRepository clubRepository;
    private final ClubMembershipService clubMembershipService;
    private final ClubMembershipRepository clubMembershipRepository;

    @Bean
    public CommandLineRunner seedDefaultData() {
        return args -> {
            seedUser();
            seedClub();
            seedMemberships();
        };
    }

    private void seedUser() {
        UserAccount existingUser = userAccountService.findByEmail(OWNER_EMAIL).orElse(null);
        if (existingUser != null) {
            log.info("Default user already exists: {}", OWNER_EMAIL);
            seedProfile(existingUser);
            return;
        }

        userAccountService.create(new UserAccountRequest(
                OWNER_EMAIL,
                OWNER_PASSWORD,
                null
        ));
        log.info("Created default user: {}", OWNER_EMAIL);

        UserAccount createdUser = userAccountService.findByEmail(OWNER_EMAIL)
                .orElseThrow(() -> new IllegalStateException("Default user was not created"));
        seedProfile(createdUser);
    }

    private void seedProfile(UserAccount userAccount) {
        if (personProfileService.findByUserAccount(userAccount).isPresent()) {
            log.info("Default person profile already exists for user: {}", OWNER_EMAIL);
            return;
        }

        personProfileService.createProfile(userAccount, new PersonProfileRequest(
                "Red",
                "Crew",
                "RedCrew",
                null
        ));
        log.info("Created default person profile for user: {}", OWNER_EMAIL);
    }

    private void seedClub() {
        try {
            clubService.createClub(new ClubRequest(
                    CLUB_NAME,
                    "redcrew.png",
                    "RedCrew Team"
            ), OWNER_EMAIL);
            log.info("Created default club: {}", CLUB_NAME);
        } catch (RuntimeException ex) {
            log.info("Default club already exists or cannot be created: {}", ex.getMessage());
        }
    }

    private void seedMemberships() {
        UUID clubExternalId = clubRepository.findByNameAndClubStatus(CLUB_NAME, ClubStatus.ACTIVE)
                .map(Club::getExternalId)
                .orElseThrow(() -> new IllegalStateException("Default club was not created"));

        seedMembership(clubExternalId, "Maciej", "Kasza", "Maciek", 6, MemberPosition.SETTER);
        seedMembership(clubExternalId, "Mateusz", "Piekarz", "Mateusz P", 0, MemberPosition.MIDDLE_BLOCKER);
        seedMembership(clubExternalId, "Marcin", "Popiela", "Marcin", 97, MemberPosition.MIDDLE_BLOCKER);
        seedMembership(clubExternalId, "Kamil", "Augustyn", "Kamil A", 3, MemberPosition.OPPOSITE);
        seedMembership(clubExternalId, "Kamil", "Korczak", "Kamil K", 10, MemberPosition.OUTSIDE_HITTER);
        seedMembership(clubExternalId, "Mateusz", "Kaczmarski", "Mateusz K", 9, MemberPosition.OUTSIDE_HITTER);
        seedMembership(clubExternalId, "Marek", "Pasieczny", "Marek", 13, MemberPosition.LIBERO);
        seedMembership(clubExternalId, "Dominik", "Kitlas", "Dominik", 26, MemberPosition.MIDDLE_BLOCKER);
    }

    private void seedMembership(UUID clubExternalId,
                                String firstName,
                                String lastName,
                                String displayName,
                                int shirtNumber,
                                MemberPosition position) {
        if (clubMembershipRepository.existsPlayer(
                clubExternalId,
                firstName,
                lastName,
                ClubStatus.ACTIVE
        )) {
            log.info("Membership already exists for {} {}", firstName, lastName);
            return;
        }

        clubMembershipService.createMembership(new ClubMembershipCreateRequest(
                clubExternalId,
                ClubMembershipRole.PLAYER,
                firstName,
                lastName,
                displayName,
                shirtNumber,
                Set.of(position),
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 6, 30)
        ), OWNER_EMAIL);
        log.info("Created membership for {} {}", firstName, lastName);
    }

}
