package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.AttachedFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IAttachedFilesRepository extends JpaRepository<AttachedFiles, Long> {

    List<AttachedFiles> findByEnabledTrue();
}
