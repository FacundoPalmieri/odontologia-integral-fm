package com.odontologiaintegralfm.feature.person.core.repository;

import com.odontologiaintegralfm.feature.person.core.model.ContactEmail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IContactEmailRepository extends JpaRepository<ContactEmail, Long> {

    Optional<ContactEmail> findByEmail(String email);


    @Query("""
    SELECT ce
    FROM ContactEmail ce
    WHERE ce NOT IN (
        SELECT ce2
        FROM Person p
        JOIN p.contactEmails ce2
        )
    """)
    List<ContactEmail> findOrphan();

}
