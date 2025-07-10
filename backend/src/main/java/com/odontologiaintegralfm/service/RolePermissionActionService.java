package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.dto.PermissionActionRequestDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Action;
import com.odontologiaintegralfm.model.Permission;
import com.odontologiaintegralfm.model.Role;
import com.odontologiaintegralfm.model.RolePermissionAction;
import com.odontologiaintegralfm.repository.IRolePermissionActionRepository;
import com.odontologiaintegralfm.service.interfaces.IPermissionService;
import com.odontologiaintegralfm.service.interfaces.IRolePermissionActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class RolePermissionActionService implements IRolePermissionActionService {
    @Autowired
    private IRolePermissionActionRepository rolePermissionActionRepository;

    @Autowired
    private IPermissionService permissionService;



    /**
     * Método para crear la relación entre Rol, permisos y acciones
     *
     * @param rolePermissionAction
     * @return
     */
    @Override
    public RolePermissionAction create(RolePermissionAction rolePermissionAction) {
        try{
            return rolePermissionActionRepository.save(rolePermissionAction);
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "RolePermissionActionService",rolePermissionAction.getRole().getId(), null, "create");
        }
    }


    /**
     * Método para obtener un listado de tipo SET, por id de Rol.
     * @param roleId : ID del Rol.
     * @return :  Set<RolePermissionAction>
     */
    @Override
    public Set<RolePermissionAction> getAllByRoleId(Long roleId) {
        try{
            return rolePermissionActionRepository.findAllByRoleId(roleId);
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "RolePermissionActionService",roleId, "", "getAllByRoleId");
        }
    }

    /**
     * Elimina las relaciones entre Rol, permisos y acciones.
     *
     * @param idRole del Rol
     */
    @Override
    public void deleteByRoleId(Long idRole) {
        try{
            rolePermissionActionRepository.deleteAllByRoleId(idRole);
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "RolePermissionActionService",idRole, "", "deleteByRoleId");
        }
    }

    /**
     * Construye las relaciones rol, permisos y acciones mediante un Rol
     *
     * @param role              : Rol recuperado desde la base de datos.
     * @param permissionActions : DTO con la lista de permisos y acciones.
     * @return
     */
    @Override
    @Transactional
    public void buildRelationByRole(Role role, Set<PermissionActionRequestDTO> permissionActions) {
        try{
            RolePermissionAction rolePermissionAction = new RolePermissionAction();

            for (PermissionActionRequestDTO permissionAction: permissionActions) {
                Permission permission = permissionService.getByIdInternal(permissionAction.getPermissionId());
                rolePermissionAction.setRole(role);
                rolePermissionAction.setPermission(permission);
                for(Action action: permission.getActions()){
                    rolePermissionAction.setAction(action);
                    this.create(rolePermissionAction);
                }
            }

        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "RolePermissionActionService",role.getId(), role.getRole(), "buildRelationByRole");
        }

    }


}
