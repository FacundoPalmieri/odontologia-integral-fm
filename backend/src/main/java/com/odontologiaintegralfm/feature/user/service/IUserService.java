package com.odontologiaintegralfm.feature.user.service;

import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.feature.authentication.dto.ResetPasswordRequestDTO;
import com.odontologiaintegralfm.feature.user.dto.UserSecCreateDTO;
import com.odontologiaintegralfm.feature.user.dto.UserSecResponseDTO;
import com.odontologiaintegralfm.feature.user.dto.UserSecUpdateDTO;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.exception.UnauthorizedException;
import com.odontologiaintegralfm.shared.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

/**
 * Interfaz que define los métodos para el servicio de gestión de usuarios.
 * Proporciona operaciones para recuperar, guardar, actualizar y eliminar usuarios,
 * así como para la gestión de contraseñas y tokens de restablecimiento.
 */
public interface IUserService {
     /**
      * Obtiene todos los usuarios.
      * @return Una respuesta que contiene una lista de objetos {@link UserSecResponseDTO} representando los usuarios.
      */
     Response<Page<UserSecResponseDTO>> getAll(int page, int size, String sortBy, String direction);

     /**
      * Obtiene un usuario por su ID.
      * @param id El ID del usuario a recuperar.
      * @return Una respuesta que contiene el objeto {@link UserSecResponseDTO} correspondiente al usuario.
      */
     Response<UserSecResponseDTO> getById(Long id);

     /**
      * Obtiene un usuario por su ID.
      * @param id El ID del usuario a recuperar.
      * @return La entidad recuperada
      */
      UserSec getByIdInternal(Long id);

     /**
      * Realiza baja lógica de un usuario, con todos las entidades relacionadas (ej: Archivos adjuntos)
      * @param id
      * @return
      */
     // UserSecResponseDTO disableById(Long id);



     /**
      * Obtiene un usuario por su username.
      * @param username El username del usuario a recuperar.
      * @return el objeto {@link UserSec} correspondiente al usuario.
      */
     UserSec getByUsername(String username);


     /**
      * Guarda un nuevo usuario junto con su información personal y, opcionalmente, su rol como dentista.
      * <p>
      * Este método realiza validaciones sobre el nombre de usuario, roles asignados y coincidencia de contraseñas.
      * También gestiona la creación de la persona asociada y, si corresponde, del dentista.
      * </p>
      *
      * @param userSecCreateDto Objeto {@link UserSecCreateDTO} con los datos del usuario, persona y dentista (opcional).
      * @return Una {@link Response} con los datos del usuario creado, incluyendo la persona y el dentista si aplica.
      * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
      * @throws ConflictException Si no se puede crear la persona asociada.
      */
     Response<UserSecResponseDTO> create(UserSecCreateDTO userSecCreateDto);


     /**
      * Encripta una contraseña.
      * @param password La contraseña en texto plano a encriptar.
      * @return La contraseña encriptada.
      */
     String encriptPassword(String password);

     /**
      * Crea un token para restablecer la contraseña de un usuario.
      * @param email El correo electrónico del usuario para el cual generar el token.
      * @return El token de restablecimiento de contraseña generado.
      */
     Response<String> createTokenResetPasswordForUser(String email);



     /**
      * Actualiza la contraseña de un usuario.
      * @param resetPasswordRequestDTO El DTO con los datos para actualizar la contraseña.
      * @param request La solicitud HTTP, necesaria para generar el contexto de la actualización.
      * @return El mensaje de éxito o error tras la actualización de la contraseña.
      */
     Response<String> updatePassword(ResetPasswordRequestDTO resetPasswordRequestDTO, HttpServletRequest request);

     /**
      * Actualiza la información de un usuario en la base de datos.
      * <p>
      * Este método realiza varias validaciones antes de actualizar los datos del usuario,
      * incluyendo restricciones sobre la actualización de usuarios con rol "DEV" y cambios
      * en el estado de la cuenta o los roles. Finalmente, guarda los cambios en la base de datos.
      * </p>
      *
      * @param userSecUpdateDTO DTO que contiene los datos actualizados del usuario.
      * @return {@link Response} que contiene el {@link UserSecResponseDTO} con los datos actualizados.
      * @throws NotFoundException  Si el usuario a actualizar no se encuentra en la base de datos.
      * @throws ConflictException  Si ocurre un error durante la actualización.
      * @throws DataBaseException Si ocurre un error de acceso a la base de datos.
      */
     Response<UserSecResponseDTO> update(UserSecUpdateDTO userSecUpdateDTO);

     /**
      * Incrementa el contador de intentos fallidos de inicio de sesión de un usuario.
      * <p>
      * Si el usuario existe en la base de datos, este método incrementa el número de intentos fallidos,
      * actualiza la fecha de última modificación y guarda los cambios en la base de datos.
      * </p>
      *
      * @param username El nombre de usuario cuyo contador de intentos fallidos será incrementado.
      * @throws DataBaseException Si ocurre un error al acceder a la base de datos durante la actualización.
      */
     void incrementFailedAttempts(String username);



     /**
      * Verifica si un usuario aún tiene intentos de inicio de sesión disponibles antes de ser bloqueado.
      * <p>
      * Este método compara el número de intentos fallidos de inicio de sesión de un usuario con el límite
      * configurado en el sistema. Si el usuario ha alcanzado o superado el límite de intentos fallidos,
      * devuelve {@code false}; de lo contrario, devuelve {@code true}.
      * </p>
      *
      * @param username El nombre de usuario cuya cantidad de intentos fallidos se verificará.
      * @return {@code true} si el usuario aún tiene intentos disponibles, {@code false} si ha alcanzado el límite.
      * @throws DataBaseException Si ocurre un error en la base de datos durante la verificación.
      */
     boolean verifyAttempts(String username);



     /**
      * Bloquea la cuenta de un usuario tras exceder el número de intentos fallidos de inicio de sesión.
      * <p>
      * Este método busca al usuario por su nombre de usuario y, si existe, establece su cuenta como bloqueada,
      * registra la fecha y hora del bloqueo y actualiza la última modificación. Luego, guarda los cambios en la base de datos.
      * </p>
      *
      * @param username El nombre de usuario cuya cuenta será bloqueada.
      * @return El objeto {@link UserSec} con la cuenta bloqueada.
      * @throws UnauthorizedException  Si el usuario no existe en la base de datos.
      * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
      */
     UserSec blockAccount(String username);



     /**
      * Verifica si la cuenta de un usuario está habilitada en el sistema.
      * <p>
      * Si el usuario existe en la base de datos, se comprueba si su cuenta está habilitada.
      * En caso de que no lo esté, se lanza una excepción {@code UnauthorizedException}.
      * </p>
      *
      * @param username El nombre de usuario cuya cuenta se desea verificar.
      * @throws UnauthorizedException Si la cuenta del usuario no está habilitada.
      * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
      */
     void enableAccount(String username);


     /**
      * Restablece el contador de intentos fallidos de inicio de sesión de un usuario a cero.
      * <p>
      * Si el usuario existe en la base de datos, este método establece su número de intentos fallidos en 0,
      * actualiza la fecha de última modificación y guarda los cambios en la base de datos.
      * </p>
      *
      * @param username El nombre de usuario cuyo contador de intentos fallidos será reiniciado.
      * @throws DataBaseException Si ocurre un error al acceder a la base de datos durante la actualización.
      */
     void resetFailedAttempts (String username);
}
