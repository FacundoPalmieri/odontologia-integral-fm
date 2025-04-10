package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.RefreshTokenRequestDTO;
import com.odontologiaintegralfm.model.RefreshToken;

public interface IRefreshTokenService {

    /**
     * Crea un nuevo Refresh Token para el usuario especificado por su nombre de usuario.
     * @param username El nombre de usuario para el cual se genera el Refresh Token.
     * @return El Refresh Token recién creado y guardado en la base de datos.
     */
    RefreshToken createRefreshToken(String username);




    /**
     * Valída el Refresh Token recibido en el DTO comparándolo con el Refresh Token almacenado en la base de datos.
     * También verifica si el Refresh Token ha expirado.
     *
     * @param refreshToken El objeto RefreshToken almacenado en la base de datos que se va a validar.
     * @param refreshTokenRequestDTO El objeto RefreshTokenRequestDTO que contiene el Refresh Token enviado por el cliente.
     */
    void validateRefreshToken(RefreshToken refreshToken, RefreshTokenRequestDTO refreshTokenRequestDTO);

    /**
     *  Elimina el Refresh Token correspondiente al usuario y token proporcionado.
     * @param refreshToken El Refresh Token que se va a eliminar.
     */
    void deleteRefreshToken(String refreshToken);

    /**
     * Elimina el Refresh Token correspondiente al ID del usuario proporcionado.
     * @param idUser el ID del usuario.
     */
    void deleteRefreshToken(Long idUser);


    /**
     * Obtiene el Refresh Token correspondiente al ID proporcionado.
     * @param id El ID del usuario.
     * @return El refresh token encontrado.
     */
    RefreshToken getRefreshTokenByUserId(Long id);

}