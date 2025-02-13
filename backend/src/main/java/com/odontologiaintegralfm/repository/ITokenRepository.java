package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.TokenConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ITokenRepository extends JpaRepository<TokenConfig, Long> {
    @Query("SELECT t. expiration FROM TokenConfig t")
    Long findFirst();

    @Modifying
    @Query("UPDATE TokenConfig t SET t.expiration = :expiration")
    void update(@Param("expiration") Long expiration);
}
