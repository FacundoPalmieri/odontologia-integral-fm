package com.odontologiaintegralfm.feature.user.repository;

import com.odontologiaintegralfm.feature.authentication.model.Role;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserSec, Long> {

    Optional<UserSec> findByIdAndEnabledTrue(Long id);

    Optional<UserSec> findUserEntityByUsername(String username);

    UserSec findByResetPasswordToken(String token);

    @Query("SELECT failedLoginAttempts  FROM UserSec WHERE username = :username")
    Integer findFailedLoginAttemptsByUsername(@Param("username") String username);


    @Query("""
    SELECT u
    FROM UserSec u
    WHERE u.person.id IS NOT NULL
    """)
    Page<UserSec> findAllExcludingDevelopers (Pageable pageable);


    @Query("""
            SELECT u.username
            FROM UserSec u
            Join u.rolesList r
            WHERE r.name IN :role
            """)
    List<String> findUsernameByRole(@Param("role") List<String> roles);

    @Query("""
    SELECT u
    FROM UserSec u
    LEFT JOIN FETCH u.person
    WHERE u.id = :id
""")
    Optional<UserSec> findByIdWithPerson(Long id);


}
