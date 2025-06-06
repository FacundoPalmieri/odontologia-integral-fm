package com.odontologiaintegralfm.repository;


import com.odontologiaintegralfm.model.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IContactPhoneRepository extends JpaRepository<ContactPhone, Long> {

    Optional<ContactPhone> findByNumber(String phone);

    @Query("""
    SELECT cp 
    FROM ContactPhone cp
    WHERE cp NOT IN (
    SELECT cp2
    FROM Person p
    JOIN p.contactPhones cp2
    )
    """)
    List<ContactPhone> findOrphan();



}
