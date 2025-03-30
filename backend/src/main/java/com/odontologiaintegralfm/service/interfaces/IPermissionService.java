package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.PermissionResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.Permission;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define los métodos para el servicio de gestión de permisos.
 * Proporciona métodos para obtener permisos, ya sea en lista o por su ID.
 */
public interface IPermissionService {


    /**
     * Obtiene todos los permisos disponibles.
     * @return Un objeto {@link Response} que contiene una lista de objetos {@link PermissionResponseDTO}
     *         con todos los permisos disponibles.
     */
    Response<List<PermissionResponseDTO>> getAll();

    /**
     * Obtiene un permiso por su ID.
     * @param id El ID del permiso que se desea obtener.
     * @return Un objeto {@link Response} que contiene el permiso correspondiente al ID especificado
     *         como un {@link PermissionResponseDTO}.
     */
    Response<PermissionResponseDTO> getById(Long id);

    /**
     * Busca un permiso en función de su ID y devuelve un {@link Optional} que puede estar vacío
     * si el permiso no se encuentra.
     *
     * @param id El ID del permiso que se desea buscar.
     * @return Un {@link Optional} que contiene el permiso si se encuentra, o está vacío si no.
     */
    Permission getByIdInternal(Long id);
 // Permission save(Permission permission);
 // void deleteById(Long id);
 // Permission update(Permission permission);

}
