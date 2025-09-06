package com.odontologiaintegralfm.feature.authentication.repository;

import com.odontologiaintegralfm.feature.authentication.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IActionRepository extends JpaRepository<Action, Long> {
}
