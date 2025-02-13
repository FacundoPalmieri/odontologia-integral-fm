package com.odontologiaintegralfm.service.interfaces;

/** Interfaz que proporciona métodos para obtener y actualizar el número de intentos fallidos de inicio de sesión.*/
public interface IFailedLoginAttemptsService {

    /**
     * Obtiene el número actual de intentos fallidos de inicio de sesión.
     * @return El número de intentos fallidos de inicio de sesión.
     */
    Integer get();

    /**
     * Actualiza el número de intentos fallidos de inicio de sesión con el valor proporcionado.
     * @param failedLoginAttempts El nuevo valor de intentos fallidos de inicio de sesión.
     */
    void update(Integer failedLoginAttempts);


}
