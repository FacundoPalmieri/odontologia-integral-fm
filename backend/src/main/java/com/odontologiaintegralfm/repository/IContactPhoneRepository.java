package com.odontologiaintegralfm.repository;


import com.odontologiaintegralfm.model.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IContactPhoneRepository extends JpaRepository<ContactPhone, Long> {

    Optional<ContactPhone> findByNumber(String phone);

    Optional<ContactPhone> findByPersonsId(Long id);


}
