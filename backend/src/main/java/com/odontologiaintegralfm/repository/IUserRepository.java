package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.UserSec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserSec, Long> {


    Optional<UserSec> findUserEntityByUsername(String username);

    UserSec findByResetPasswordToken(String token);

    @Query("SELECT failedLoginAttempts  FROM UserSec WHERE username = :username")
    Integer findFailedLoginAttemptsByUsername(@Param("username") String username);


    @Query("""
    SELECT u
    FROM UserSec u
    WHERE NOT EXISTS (
    SELECT r FROM u.rolesList r WHERE r.role = :nameRoleDev
    )
    """)
    Page<UserSec> findAllExcludingDevelopers (@Param("nameRoleDev") String nameRoleDev ,Pageable pageable);

}
