package pl.volleyflow.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)),
            60_000);

    @Test
    void shouldGenerateAndValidateTokenForActiveUser() {
        UserAccount user = user();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void shouldRejectTokenForUnverifiedUser() {
        UserAccount user = user();
        String token = jwtService.generateToken(user);
        user.setEmailVerified(false);

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }

    @Test
    void shouldRejectTokenForAnotherUser() {
        UserAccount user = user();
        String token = jwtService.generateToken(user);
        user.setEmail("someone-else@example.com");

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }

    @Test
    void shouldRejectInvalidSignature() {
        String token = jwtService.generateToken(user());
        JwtService anotherService = new JwtService(Base64.getEncoder().encodeToString(
                "abcdef0123456789abcdef0123456789".getBytes(StandardCharsets.UTF_8)), 60_000);

        assertThrows(JwtException.class, () -> anotherService.extractUsername(token));
    }

    private UserAccount user() {
        return UserAccount.builder().externalId(UUID.randomUUID()).email("user@example.com")
                .status(UserAccountStatus.ACTIVE).emailVerified(true).build();
    }
}
