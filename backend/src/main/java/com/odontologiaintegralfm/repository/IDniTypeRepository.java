package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.DniType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDniTypeRepository extends JpaRepository<DniType, Long> {

    List<DniType> findAll();
}
