package com.odontologiaintegralfm.repository;


import com.odontologiaintegralfm.model.Role;
import com.odontologiaintegralfm.model.RolePermissionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Set;


@Repository
public interface IRolePermissionActionRepository extends JpaRepository<RolePermissionAction, Long> {

   @Query("""
    SELECT rpa
    FROM RolePermissionAction rpa
    JOIN FETCH rpa.permission p
    JOIN FETCH rpa.action a
    WHERE rpa.role.id = :roleId
    """)
    Set<RolePermissionAction> findAllByRoleId(@Param("roleId") Long roleId);

   void deleteAllByRoleId(Long roleId);
}
