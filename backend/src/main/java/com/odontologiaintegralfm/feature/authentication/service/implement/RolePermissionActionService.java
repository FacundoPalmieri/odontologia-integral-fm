package com.odontologiaintegralfm.feature.authentication.service.implement;


import com.odontologiaintegralfm.feature.authentication.dto.PermissionActionRequestDTO;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.feature.authentication.model.Action;
import com.odontologiaintegralfm.feature.authentication.model.Permission;
import com.odontologiaintegralfm.feature.authentication.model.Role;
import com.odontologiaintegralfm.feature.authentication.model.RolePermissionAction;
import com.odontologiaintegralfm.feature.authentication.repository.IRolePermissionActionRepository;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IPermissionService;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IRolePermissionActionService;
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
    @Autowired
    private ActionService actionService;


    /**
     * Método para crear la relación entre Rol, permisos y acciones
     *
     * @param rolePermissionAction
     * @return
     */
    @Override
    @Transactional
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
    @Transactional
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
                for(Long idAction: permissionAction.getActionId()){
                    Action action = actionService.getById(idAction);
                    rolePermissionAction.setAction(action);
                    this.create(rolePermissionAction);
                }
            }

        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "RolePermissionActionService",role.getId(), role.getName(), "buildRelationByRole");
        }

    }


}
