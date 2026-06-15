package pl.volleyflow.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.club.model.Club;

import java.util.Optional;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByNameAndActiveTrue(String name);

    @Query(value = """
            select *
            from app.club c
            where c.external_id = :externalId
              and c.active = true
            """, nativeQuery = true)
    Optional<Club> findByExternalId(@Param("externalId") UUID externalId);

    Optional<Club> findByNameAndActiveTrue(String name);

    @Modifying
    @Transactional
    @Query(value = """
            update app.club
            set active = false,
                updated_at = now()
            where id = :clubId
            """, nativeQuery = true)
    void setActiveFalseById(@Param("clubId") long clubId);

}
