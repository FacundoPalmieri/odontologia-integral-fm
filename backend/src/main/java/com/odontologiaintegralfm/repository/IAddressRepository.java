package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Address;
import com.odontologiaintegralfm.model.Locality;
import com.odontologiaintegralfm.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface IAddressRepository extends JpaRepository<Address, Long> {

    @Query("""
    SELECT a
    FROM Address a 
    WHERE a.street = :street 
        AND a.number = :number 
        AND a.locality = :locality 
        AND ((:floor IS NULL AND a.floor IS NULL) OR a.floor = :floor)
        AND ((:apartment IS NULL AND a.apartment IS NULL) OR a.apartment = :apartment)
    """)
    Optional<Address> findAddressComplete(
            @Param("street") String street,
            @Param("number") Integer number,
            @Param("floor") String floor,
            @Param("apartment") String apartment,
            @Param("locality") Locality locality);


    @Query("""
     SELECT p
     FROM Person p
     JOIN FETCH p.address a
     WHERE p.address.id = :addressOld
     AND p.enabled = true
     """)
    Optional<Patient> findByAddressIdOld(@Param("addressOld") Long addressOld);


    @Query("""
    SELECT p.address
    FROM Person p
    WHERE p.id = :personId
    AND p.enabled = true
    """)
    Address findByPersonId(@Param("personId") Long id);

}
