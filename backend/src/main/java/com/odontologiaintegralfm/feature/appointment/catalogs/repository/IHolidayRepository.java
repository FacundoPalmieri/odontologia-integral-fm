package com.odontologiaintegralfm.feature.appointment.catalogs.repository;

import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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
     * @return
     */
    List<Holiday> findAllByYear(int year);


    /**
     * Consulta feriado por una fecha puntual
     * @param date
     * @return
     */
    Optional<Holiday> findByDate(LocalDate date);

}
