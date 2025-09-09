package com.odontologiaintegralfm.feature.appointment.core.repository;


import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface IDentistAvailabilityRepository extends JpaRepository<DentistAvailability,Long> {

   List<DentistAvailability> findAllByDentistId(Long id);
}
