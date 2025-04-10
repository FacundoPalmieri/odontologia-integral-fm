package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String token);

    Optional<RefreshToken> findByUser_Id(Long id);

    @Modifying
    @Query("DELETE RefreshToken t WHERE t.user.id =:userId")
    void deleteByUserId(@Param("userId") Long userId);
}
