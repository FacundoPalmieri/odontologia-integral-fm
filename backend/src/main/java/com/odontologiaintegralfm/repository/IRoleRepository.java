package com.odontologiaintegralfm.repository;
import com.odontologiaintegralfm.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleEntityByRole(String role);

   @Query("""
    SELECT  r
    FROM Role r
    WHERE r.role != :role
    """)
   Set<Role> findAllExcludingDevelopers(@Param("role") String role);

}
