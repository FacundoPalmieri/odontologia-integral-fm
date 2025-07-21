package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.AttachedFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IAttachedFilesRepository extends JpaRepository<AttachedFiles, Long> {

   Optional <List<AttachedFiles>> findAllByPersonIdAndEnabledTrue(Long personId);


}
