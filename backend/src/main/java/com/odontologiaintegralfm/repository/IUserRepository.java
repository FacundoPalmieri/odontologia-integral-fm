package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.UserSec;
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


}
