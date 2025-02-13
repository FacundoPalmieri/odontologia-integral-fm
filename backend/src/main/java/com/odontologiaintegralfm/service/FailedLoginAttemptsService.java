package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.repository.IFailedLoginAttemptsRepository;
import com.odontologiaintegralfm.service.interfaces.IFailedLoginAttemptsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;


/**
 * Servicio para gestionar los intentos fallidos de inicio de sesión.
 * <p>
 * Esta clase proporciona métodos para obtener y actualizar el número de intentos fallidos de inicio de sesión
 * en la base de datos. Utiliza el repositorio {@link IFailedLoginAttemptsRepository} para interactuar con
 * la base de datos y maneja posibles errores de acceso a la misma mediante excepciones
 * {@link DataBaseException}.
 * </p>
 */

@Service
public class FailedLoginAttemptsService implements IFailedLoginAttemptsService {

    @Autowired
    private IFailedLoginAttemptsRepository failedLoginAttemptsRepository;


    /**
     * Obtiene el número de intentos fallidos de inicio de sesión.
     * <p>
     * Este método consulta el repositorio {@link IFailedLoginAttemptsRepository} para obtener el primer valor disponible
     * del número de intentos fallidos de inicio de sesión. Si ocurre un error en la base de datos durante la consulta,
     * se lanza una excepción {@link DataBaseException}.
     * </p>
     * @return El número de intentos fallidos de inicio de sesión.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public Integer get() {
        try{
           return failedLoginAttemptsRepository.findFirst();
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "FailedLoginAttemptsService",1L, "", "getAttempt");
        }
    }




    /**
     * Actualiza el número de intentos fallidos de inicio de sesión.
     * <p>
     * Este método actualiza el valor del número de intentos fallidos en la base de datos utilizando el repositorio
     * {@link IFailedLoginAttemptsRepository}. Si ocurre un error en la base de datos durante la actualización,
     * se lanza una excepción {@link DataBaseException}.
     * </p>
     * @param failedLoginAttempts El nuevo valor del número de intentos fallidos a actualizar.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
     */
    @Override
    @Transactional
    public void update(Integer failedLoginAttempts) {
        try {
            failedLoginAttemptsRepository.update(failedLoginAttempts);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "FailedLoginAttemptsService",1L, "", "getAttempt");
        }
    }
}
