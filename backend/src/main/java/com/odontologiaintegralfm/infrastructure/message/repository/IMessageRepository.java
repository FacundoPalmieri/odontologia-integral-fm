package com.odontologiaintegralfm.infrastructure.message.repository;

import com.odontologiaintegralfm.infrastructure.message.model.MessageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMessageRepository extends JpaRepository<MessageConfig, Long> {

    MessageConfig findByKeyAndLocale(String clave, String locale);
}
