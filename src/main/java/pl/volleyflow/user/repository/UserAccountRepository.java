package pl.volleyflow.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.user.entity.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<UserAccount> findByEmail(String email);

}
