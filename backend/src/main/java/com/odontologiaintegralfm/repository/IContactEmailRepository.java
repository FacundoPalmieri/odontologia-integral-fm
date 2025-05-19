package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface IContactEmailRepository extends JpaRepository<ContactEmail, Long> {

    Optional<ContactEmail> findByEmail(String email);

    Optional<ContactEmail> findByPersonsId(Long id);

}
