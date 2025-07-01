package com.odontologiaintegralfm.repository;
import com.odontologiaintegralfm.model.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleEntityByRole(String role);

   @Query("""
    SELECT  r
    FROM Role r
    WHERE r.role != :role
    """)
    List<Role> findAllExcludingDevelopers(@Param("role") String role);

}
