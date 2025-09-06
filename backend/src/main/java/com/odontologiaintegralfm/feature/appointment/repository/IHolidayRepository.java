package com.odontologiaintegralfm.feature.appointment.repository;

import com.odontologiaintegralfm.feature.appointment.model.Holiday;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;


@Repository
public interface IHolidayRepository extends JpaRepository<Holiday, Long> {

    /**
     * Método que retorna true si existe algún feriado en la base con el año consultado.
     * @param year
     * @return
     */
    int countByYear(@Param("year") int year);



    /**
     * Obtiene todos los feriados por año.
     * @param year
     * @param pageable
     * @return
     */
    Page<Holiday> findAllByYear(int year, Pageable pageable);


    /**
     * Consulta feriado por una fecha puntual
     * @param date
     * @return
     */
    Optional<Holiday> findByDate(LocalDate date);

}
