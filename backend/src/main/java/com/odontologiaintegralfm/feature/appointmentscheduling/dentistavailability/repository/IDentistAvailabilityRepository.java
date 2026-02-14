package com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.repository;


import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
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
