package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IGenderRepository extends JpaRepository<Gender, Long> {

    List<Gender> findAllByEnabledTrue();

    Optional<Gender> findByIdAndEnabledTrue(Long id);
}
