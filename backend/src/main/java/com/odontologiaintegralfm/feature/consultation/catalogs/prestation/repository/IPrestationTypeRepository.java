package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPrestationTypeRepository extends JpaRepository<PrestationType, Long> {
}
