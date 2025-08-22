package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.PermissionActionRequestDTO;
import com.odontologiaintegralfm.model.Role;
import com.odontologiaintegralfm.model.RolePermissionAction;
import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public interface IRolePermissionActionService {


    /**
     * Método para crear la relación entre Rol, permisos y acciones
     * @param rolePermissionAction
     * @return
     */
    RolePermissionAction create(RolePermissionAction rolePermissionAction);



    /**
     * Método para obtener un listado de tipo SET, por id de Rol.
     * @param roleId : ID del Rol.
     * @return :  Set<RolePermissionAction>
     */
    Set<RolePermissionAction> getAllByRoleId(Long roleId);


    /**
     * Elimina las relaciones entre Rol, permisos y acciones.
     * @param idRole del Rol
     */
    void deleteByRoleId(Long idRole);


    /**
     * Construye las relaciones rol, permisos y acciones mediante un Rol
     * @param role: Rol recuperado desde la base de datos.
     * @param permissionActions: DTO con la lista de permisos y acciones.
     */
    void buildRelationByRole(Role role, Set<PermissionActionRequestDTO> permissionActions);
}
