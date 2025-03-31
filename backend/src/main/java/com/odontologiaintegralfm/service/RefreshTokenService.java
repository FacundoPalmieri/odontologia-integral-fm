package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.RefreshTokenDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.RefreshTokenException;
import com.odontologiaintegralfm.exception.TokenInvalidException;
import com.odontologiaintegralfm.model.RefreshToken;
import com.odontologiaintegralfm.repository.IRefreshTokenRepository;
import com.odontologiaintegralfm.service.interfaces.IRefreshTokenService;
import com.odontologiaintegralfm.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService implements IRefreshTokenService {

    @Autowired
    IRefreshTokenRepository refreshTokenRepository;

    @Autowired
    IUserService userService;


    /**
     * Crea un nuevo Refresh Token para el usuario especificado por su nombre de usuario.
     * <p>
     * Este método genera un Refresh Token único utilizando un UUID, lo asocia con el usuario
     * que se obtiene mediante el nombre de usuario, y establece las fechas de creación y expiración.
     * Finalmente, guarda el Refresh Token en la base de datos.
     * </p>
     *
     * @param username El nombre de usuario para el cual se genera el Refresh Token.
     * @return El Refresh Token recién creado y guardado en la base de datos.
     */
    @Override
    public RefreshToken createRefreshToken(String username) {

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken.setUser(userService.getByUsername(username));
        refreshToken.setCreatedDate(LocalDateTime.now());
        refreshToken.setExpirationDate(LocalDateTime.now().plusDays(15));
        return refreshTokenRepository.save(refreshToken);
    }




    /**
     * Valída el Refresh Token recibido en el DTO comparándolo con el Refresh Token almacenado en la base de datos.
     * También verifica si el Refresh Token ha expirado.
     *
     * @param refreshToken El objeto RefreshToken almacenado en la base de datos que se va a validar.
     * @param refreshTokenDTO El objeto RefreshTokenDTO que contiene el Refresh Token enviado por el cliente.
     * @throws RefreshTokenException Si el código del Refresh Token no coincide con el almacenado o si el Refresh Token ha expirado.
     * <p>
     * La excepción {@link RefreshTokenException} se lanza con los siguientes códigos de error:
     * <ul>
     *   <li><b>userDetailServiceImpl.refreshToken.invalidCode</b>: Si el código del Refresh Token es inválido.</li>
     *   <li><b>userDetailServiceImpl.refreshToken.refreshTokenExpired</b>: Si el Refresh Token ha expirado.</li>
     * </ul>
     * </p>
     */
    @Override
    public void validateRefreshToken(RefreshToken refreshToken, RefreshTokenDTO refreshTokenDTO) {
        //Valída el código
        if(!refreshTokenDTO.getRefreshToken().equals(refreshToken.getRefreshToken())){
            throw new RefreshTokenException(refreshTokenDTO.getUser_id(),"RefreshTokenService", "validateRefreshToken", "userDetailServiceImpl.refreshToken.invalidCode");
        }

        //Valida la vigencia
        if(refreshToken.getExpirationDate().isBefore(LocalDateTime.now())){
            throw new RefreshTokenException(refreshToken.getUser().getId(), "RefreshTokenService","validateRefreshToken","userDetailServiceImpl.refreshToken.refreshTokenExpired");
        }

    }



    /**
     * Elimina el Refresh Token correspondiente al usuario y token proporcionado.
     *
     * Este método busca el Refresh Token en la base de datos usando el token proporcionado.
     * Si el Refresh Token existe, lo elimina; en caso contrario, lanza una excepción {@link TokenInvalidException}.
     * En caso de un error de acceso a la base de datos o problemas de transacción, lanza una {@link DataBaseException}.
     *
     * @param refreshToken El Refresh Token que se va a eliminar.
     * @throws TokenInvalidException Si el Refresh Token no se encuentra en la base de datos.
     * @throws DataBaseException Si ocurre un error en la base de datos o en la transacción.
     */
    @Override
    public void deleteRefreshToken(String refreshToken) {
        try{
            RefreshToken refreshTokenDB = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(()-> new RefreshTokenException(0L,"Refresh Token Service", "deleteRefreshToken", "refreshTokenService.deleteRefreshToken"));
            refreshTokenRepository.delete(refreshTokenDB);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "RefreshTokenService",0L, "", "deleteRefreshTokenByUsername");
        }
    }

    /**
     * Elimina el Refresh Token correspondiente al ID del usuario proporcionado.
     *
     * @param userId el ID del usuario.
     */
    @Transactional
    @Override
    public void deleteRefreshToken(Long userId) {
        try{
            refreshTokenRepository.deleteByUserId(userId);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "RefreshTokenService",userId, "", "deleteRefreshToken");
        }
    }



    /**
     * Obtiene el Refresh Token correspondiente al ID proporcionado.
     *
     * Este método busca un Refresh Token en la base de datos utilizando el ID del usuario.
     * Si el refresh token existe, lo retorna, en caso contrario, lanza una excpeción {@link RefreshTokenException}.
     * En caso de un error de acceso a la base de datos o problemas de transacción, lanza una {@link DataBaseException}.
     *
     * @param id El ID del usuario.
     * @return El refresh token encontrado.
     * @throws RefreshTokenException Si el Refresh Token no se encuentra en la base de datos.
     * @throws DataBaseException Si ocurre un error en la base de datos o en la transacción.
     */
    public RefreshToken getRefreshTokenByUserId(Long id) {
        try{
            return refreshTokenRepository.findByUser_Id(id).orElseThrow(()-> new RefreshTokenException(id, "Refresh Token Service", "getRefreshTokenByUserId", "No se encontró refreshToken asociado al usuario."));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "RefreshTokenService",0L, "", "getRefreshTokenByUserId");
        }
    }
}