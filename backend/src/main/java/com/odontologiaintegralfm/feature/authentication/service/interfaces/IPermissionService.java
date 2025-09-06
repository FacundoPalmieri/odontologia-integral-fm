package com.odontologiaintegralfm.feature.authentication.service.interfaces;

import com.odontologiaintegralfm.feature.authentication.dto.PermissionFullResponseDTO;
import com.odontologiaintegralfm.feature.authentication.dto.PermissionSimpleResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.authentication.model.Permission;
import java.util.List;
import java.util.Optional;


/**
 * Interfaz que define los métodos para el servicio de gestión de permisos.
 * Proporciona métodos para obtener permisos, ya sea en lista o por su ID.
 */
public interface IPermissionService {


    /**
     * Obtiene todos los permisos disponibles.
     * @return Un objeto {@link Response} que contiene una lista de objetos {@link PermissionFullResponseDTO}
     *         con todos los permisos disponibles.
     */
    Response<List<PermissionSimpleResponseDTO>> getAll();



    /**
     * Busca un permiso en función de su ID y devuelve un {@link Optional} que puede estar vacío
     * si el permiso no se encuentra.
     *
     * @param id El ID del permiso que se desea buscar.
     * @return Un {@link Optional} que contiene el permiso si se encuentra, o está vacío si no.
     */
    Permission getByIdInternal(Long id);


}
