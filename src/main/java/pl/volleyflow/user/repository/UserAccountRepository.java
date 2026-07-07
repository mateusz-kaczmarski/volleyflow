package pl.volleyflow.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.user.entity.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<UserAccount> findByEmail(String email);

}
