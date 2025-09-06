package com.odontologiaintegralfm.feature.person.core.repository;

import com.odontologiaintegralfm.feature.person.core.model.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IAttachedFilesRepository extends JpaRepository<AttachedFile, Long> {

   Optional<AttachedFile> findByIdAndEnabledTrue(Long documentId);

   Optional <List<AttachedFile>> findAllByPersonIdAndEnabledTrue(Long personId);


   @Query("""
   SELECT af
   FROM AttachedFile af
   WHERE af.enabled = false
   AND af.disabledAt < :deadline
   """)
   List<AttachedFile> findDisabled(@Param("deadline") LocalDateTime deadline);



}
