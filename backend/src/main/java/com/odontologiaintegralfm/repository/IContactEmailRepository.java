package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.ContactEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IContactEmailRepository extends JpaRepository<ContactEmail, Long> {
}
