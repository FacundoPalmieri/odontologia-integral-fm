package com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces;


import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.infrastructure.systemparameter.model.SystemParameter;
import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface ISystemParameterService {

    /**
     * Obtiene todas las parametrizaciones del sistema.
     * @return  List<SystemParameter>
     */
    List<SystemParameter> getAll();

    /**
     * Obtiene un objeto parámetro de sistema por su ID.
     * @param id del parámetro.
     * @return SystemParameter
     */
    SystemParameter getById(Long id);


    /**
     * Obtiene valor para por key de parámetro.
     * Se utiliza en métodos internos. Ej: Obtener en el JWTUtil el tiempo de expiración del JWT.
     * @param key
     * @return
     */
    String getByKey(SystemParameterKey key);

    /**
     * Actualiza valor de un parámetro del sistema.
     * @param systemParameter Objeto a actualizar.
     * @return SystemParameter
     */
    SystemParameter update(SystemParameter systemParameter);
}
