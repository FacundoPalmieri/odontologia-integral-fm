package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStepRepository extends JpaRepository<Step, Long> {
}
