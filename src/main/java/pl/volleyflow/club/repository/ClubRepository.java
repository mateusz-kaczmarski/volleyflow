package pl.volleyflow.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.volleyflow.club.model.Club;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByName(String name);

    @Query(value = """
            select *
            from app.club c
            where c.external_id = :externalId
            """, nativeQuery = true)
    Optional<Club> findByExternalId(@Param("externalId") UUID externalId);

    @Query(value = """
            select c.*
            from app.club c
            join app.user_account ua on ua.id = c.owner_id
            where ua.external_id = :userExternalId
            """, nativeQuery = true)
    List<Club> findAllByUserExternalId(@Param("userExternalId") UUID userExternalId);

}
