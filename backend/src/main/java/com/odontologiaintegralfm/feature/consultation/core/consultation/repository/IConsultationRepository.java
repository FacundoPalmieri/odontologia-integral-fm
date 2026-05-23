package com.odontologiaintegralfm.feature.consultation.core.consultation.repository;


import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IConsultationRepository extends JpaRepository<Consultation, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT c FROM Consultation c " +
            "JOIN FETCH c.patient pat JOIN FETCH pat.person " +
            "JOIN FETCH c.dentist den JOIN FETCH den.person " +
            "WHERE c.appointment.date >= :startOfDay AND c.appointment.date < :endOfDay")
    List<Consultation> findAllByDate(@Param("startOfDay") LocalDateTime startOfDay,
                                     @Param("endOfDay") LocalDateTime endOfDay);
}
