package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Nationality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INationalityRepository extends JpaRepository<Nationality, Long> {
    List<Nationality> findAllByEnabledTrue();

    Optional<Nationality> findByIdAndEnabledTrue(Long id);
}
