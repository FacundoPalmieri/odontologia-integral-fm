package com.odontologiaintegralfm.feature.authentication.repository;

import com.odontologiaintegralfm.feature.authentication.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, Long> {

}
