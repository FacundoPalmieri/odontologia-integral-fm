package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.RoleFullResponseDTO;
import com.odontologiaintegralfm.dto.RoleRequestDTO;
import com.odontologiaintegralfm.dto.RoleSimpleResponseDTO;
import com.odontologiaintegralfm.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Interfaz que define los métodos para el servicio de gestión de roles.
 * Proporciona métodos para obtener, guardar y buscar roles en el sistema.
 */
public interface IRoleService {
    /**
     * Obtiene todos los roles disponibles en el sistema.
     * @return Un objeto {@link Response} que contiene una lista de objetos {@link Role}
     *         con todos los roles disponibles en el sistema.
     */
    Response<Set<RoleSimpleResponseDTO>> getAll();



    /**
     * Método para obtener los permisos y acciones asociados a un Rol específico.
     * A partir del Id de un rol del usuario, este método obtiene todas las combinaciones
     * {@code RolePermissionAction} asociadas a cada rol, y agrupa las acciones por permiso
     * y los permisos por rol. El resultado es una lista de {@code RoleFullResponseDTO}, donde cada
     * rol contiene sus permisos y cada permiso contiene sus acciones correspondientes.
     *
     * <p>Ejemplo de estructura devuelta:</p>
     * <pre>
     * [
     *   {
     *     "id": 1,
     *     "name": "ADMIN",
     *     "permissionsList": [
     *       {
     *         "id": 10,
     *         "permission": "USERS",
     *         "name": "Gestión de usuarios",
     *         "actions": [
     *           { "id": 100, "action": "READ" },
     *           { "id": 101, "action": "WRITE" }
     *         ]
     *       }
     *     ]
     *   }
     * ]
     * </pre>
     *
     * @param idRole
     * @return
     */
    RoleFullResponseDTO getFullByRoleId(Long idRole);



    /**
     * Busca un rol en función de su ID y devuelve un {@link Optional} que puede estar vacío
     * si el rol no se encuentra.
     *
     * @param id El ID del rol que se desea buscar.
     * @return Un {@link Optional} que contiene el rol si se encuentra, o está vacío si no.
     */
    Role getByIdInternal(Long id);



    /**
     * Guarda un nuevo rol o actualiza uno existente en el sistema.
     *
     * @param roleRequestDto El objeto {@link RoleRequestDTO} que contiene los datos del rol a guardar.
     * @return Un objeto {@link Response} que contiene el rol guardado o actualizado como un
     *         {@link RoleFullResponseDTO}.
     */
    Response<RoleFullResponseDTO> create(RoleRequestDTO roleRequestDto);




    /**
     * Actualiza la lista de permisos para el rol.
     *
     * @param roleRequestDto {@link RoleRequestDTO} que contiene la lista de permisos.
     * @return Un objeto {@link Response} que contiene el rol actualizado como un{@link RoleFullResponseDTO}
     */
    Response<RoleFullResponseDTO> update (RoleRequestDTO roleRequestDto);



}
