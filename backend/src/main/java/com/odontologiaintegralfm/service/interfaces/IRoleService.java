package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.RoleDTO;
import com.odontologiaintegralfm.dto.RoleResponseDTO;
import com.odontologiaintegralfm.model.Role;

import java.util.List;
import java.util.Optional;
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
    Response<List<Role>> getAll();




    /**
     * Obtiene un rol por su ID.
     *
     * @param id El ID del rol que se desea obtener.
     * @return Un objeto {@link Response} que contiene el rol correspondiente al ID especificado
     *         como un {@link RoleResponseDTO}.
     */
    Response<RoleResponseDTO> getById(Long id);



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
     * @param roleDto El objeto {@link RoleDTO} que contiene los datos del rol a guardar.
     * @return Un objeto {@link Response} que contiene el rol guardado o actualizado como un
     *         {@link RoleResponseDTO}.
     */
    Response<RoleResponseDTO> save(RoleDTO roleDto);




    /**
     * Actualiza la lista de permisos para el rol.
     *
     * @param roleDto {@link RoleDTO} que contiene la lista de permisos.
     * @return Un objeto {@link Response} que contiene el rol actualizado como un{@link RoleResponseDTO}
     */
    Response<RoleResponseDTO> update (RoleDTO roleDto);



}
