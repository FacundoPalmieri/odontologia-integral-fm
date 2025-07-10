package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IActionRepository extends JpaRepository<Action, Long> {
}
