package com.odontologiaintegralfm.feature.person.core.repository;

import com.odontologiaintegralfm.feature.person.core.model.Address;
import com.odontologiaintegralfm.feature.person.catalogs.model.Locality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
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
     SELECT a.id
     FROM Address a
     WHERE NOT EXISTS(
        SELECT p
        FROM Person p
        WHERE p.address = a
        )
     """)
    List<Long> findOrphan();


    @Modifying
    @Query("""
    DELETE FROM Address  a
    WHERE a.id IN : ids
    """)
    int deleteAll(@Param("ids") List <Long> ids);

}
