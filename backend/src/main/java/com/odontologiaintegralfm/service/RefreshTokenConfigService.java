package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.RefreshTokenConfigRequestDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.RefreshTokenConfig;
import com.odontologiaintegralfm.repository.IRefreshTokenConfigRepository;
import com.odontologiaintegralfm.service.interfaces.IRefreshTokenConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.Optional;

/**
 * Servicio encargado de la parametrización de Refresh tokens en la aplicación.
 * <p>
 * Proporciona métodos para obtener y actualizar la fecha de expiración del Refresh token
 * almacenado en la base de datos. Maneja errores de acceso a datos y transacciones,
 * lanzando excepciones específicas en caso de fallos.
 * </p>
 *
 * <p>Este servicio implementa la interfaz {@link IRefreshTokenConfigService}.</p>
 */

@Service
public class RefreshTokenConfigService implements IRefreshTokenConfigService {

    @Autowired
    private IRefreshTokenConfigRepository refreshTokenConfigRepository;

    /**
     * Obtiene la cantidad de días del Refresh token almacenado en la base de datos.
     * <p>
     * Este método consulta el repositorio de Refresh tokens para recuperar la cantidad de días
     * del Refresh token disponible. En caso de error de acceso a la base de datos o de transacción,
     * se lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @return Un {@link Long} que representa la cantidad de días
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */
    @Override
    public Long getExpiration() {
        try {
            Optional<RefreshTokenConfig> optional = refreshTokenConfigRepository.findFirstByOrderByIdAsc();
            if (optional.isPresent()) {
                return optional.get().getExpiration();
            }

            throw new NotFoundException("exception.refreshTokenConfigNotFoundException.user",null,"exception.refreshTokenConfigNotFoundException.log",new Object[]{"Refresh Token Config Service", "getExpiration"}, LogLevel.ERROR);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "RefreshTokenConfigService", 1L, "RefreshToken", "getExpiration");
        }
    }

    /**
     * Actualiza la expiración del Refresh Token en días
     *
     * @param refreshTokenConfigRequestDTO El nuevo valor de la duración de expiración del token en días.
     * @return El tiempo de expiración actualizado.
     */
    @Override
    public int updateExpiration(RefreshTokenConfigRequestDTO refreshTokenConfigRequestDTO) {
        try {
           return refreshTokenConfigRepository.update(refreshTokenConfigRequestDTO.expiration());

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "RefreshTokenConfigService", 1L, "RefreshToken", "getExpiration");
        }
    }
}