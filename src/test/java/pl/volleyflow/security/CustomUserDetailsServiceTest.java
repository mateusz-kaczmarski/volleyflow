package pl.volleyflow.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldLoadUserByNormalizedEmail() {
        UserAccount user = UserAccount.builder().email("user@example.com").passwordHash("hash")
                .status(UserAccountStatus.ACTIVE).emailVerified(true).build();
        when(userAccountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        var details = userDetailsService.loadUserByUsername(" USER@EXAMPLE.COM ");

        assertThat(details.getUsername()).isEqualTo("user@example.com");
        assertThat(details.isEnabled()).isTrue();
        verify(userAccountRepository).findByEmail("user@example.com");
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userAccountRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com"));
    }
}
