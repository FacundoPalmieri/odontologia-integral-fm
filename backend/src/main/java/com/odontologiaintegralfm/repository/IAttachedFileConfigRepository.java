package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.AttachedFileConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
public interface IAttachedFileConfigRepository extends JpaRepository<AttachedFileConfig, Long> {

    Optional<AttachedFileConfig> findFirstBy();

}
