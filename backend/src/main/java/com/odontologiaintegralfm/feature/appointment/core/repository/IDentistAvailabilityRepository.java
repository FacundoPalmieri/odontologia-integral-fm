package com.odontologiaintegralfm.feature.appointment.core.repository;


import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface IDentistAvailabilityRepository extends JpaRepository<DentistAvailability,Long> {

   List<DentistAvailability> findAllByDentistIdAndEnabledTrue(Long id);


   @Query("""
    select da.appointmentDuration
    from DentistAvailability da
    where da.id = :id
""")
   Integer findAppointmentDurationById(@Param("id") Long idDentistAvailability);

}
