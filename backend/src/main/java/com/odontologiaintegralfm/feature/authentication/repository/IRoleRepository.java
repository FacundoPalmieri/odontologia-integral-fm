package com.odontologiaintegralfm.feature.authentication.repository;
import com.odontologiaintegralfm.feature.authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleEntityByName(String role);

   @Query("""
    SELECT  r
    FROM Role r
    WHERE r.id != :id
    """)
   Set<Role> findAllExcludingDevelopers(@Param("id") Long id);

}
