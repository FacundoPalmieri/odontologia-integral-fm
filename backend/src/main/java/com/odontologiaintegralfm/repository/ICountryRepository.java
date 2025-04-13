package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface ICountryRepository extends JpaRepository<Country, Long> {

}
