package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.RefreshTokenConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface IRefreshTokenConfigRepository extends JpaRepository<RefreshTokenConfig, Long> {

    Optional<RefreshTokenConfig> findFirstByOrderByIdAsc();

    @Transactional
    @Modifying
    @Query("UPDATE RefreshTokenConfig t SET t.expiration =:expiration")
    int update(@Param("expiration") Long expiration);

}
