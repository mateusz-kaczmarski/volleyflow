package pl.volleyflow.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record UserPrincipal(
        Long id,
        UUID externalId,
        String email,
        String passwordHash,
        UserAccountStatus status,
        boolean emailVerified
) implements UserDetails {

    private static final List<GrantedAuthority> AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    public static UserPrincipal from(UserAccount userAccount) {
        return new UserPrincipal(
                userAccount.getId(),
                userAccount.getExternalId(),
                userAccount.getEmail(),
                userAccount.getPasswordHash(),
                userAccount.getStatus(),
                userAccount.isEmailVerified()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AUTHORITIES;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !UserAccountStatus.BLOCKED.equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified && UserAccountStatus.ACTIVE.equals(status);
    }

}
