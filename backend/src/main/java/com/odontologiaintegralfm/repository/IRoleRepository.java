package com.odontologiaintegralfm.repository;
import com.odontologiaintegralfm.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleEntityByRole(String role);
}
