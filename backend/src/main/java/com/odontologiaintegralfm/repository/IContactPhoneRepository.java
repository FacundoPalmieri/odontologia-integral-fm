package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IContactPhoneRepository extends JpaRepository<ContactPhone, Long> {
}
