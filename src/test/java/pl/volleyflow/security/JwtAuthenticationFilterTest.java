package pl.volleyflow.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.service.UserAccountService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipRequestWithoutBearerToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userAccountService, userDetailsService);
    }

    @Test
    void shouldSkipInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        when(jwtService.extractUsername("invalid")).thenThrow(new IllegalArgumentException("Invalid token"));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userAccountService, userDetailsService);
    }

    @Test
    void shouldAuthenticateValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid");
        UserAccount user = UserAccount.builder().id(1L).externalId(UUID.randomUUID())
                .email("user@example.com").status(UserAccountStatus.ACTIVE).emailVerified(true).build();
        when(jwtService.extractUsername("valid")).thenReturn("user@example.com");
        when(userAccountService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("valid", user)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(UserPrincipal.from(user));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@example.com");
        verify(userDetailsService).loadUserByUsername("user@example.com");
    }
}
