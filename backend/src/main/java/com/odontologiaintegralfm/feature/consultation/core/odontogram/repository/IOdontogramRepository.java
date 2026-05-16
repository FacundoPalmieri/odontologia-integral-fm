package com.odontologiaintegralfm.feature.consultation.core.odontogram.repository;


import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOdontogramRepository extends JpaRepository<Odontogram, Long> {
}