package com.odontologiaintegralfm.feature.user.service;

import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.feature.authentication.dto.ResetPasswordRequestDTO;
import com.odontologiaintegralfm.feature.user.dto.UserSecCreateDTO;
import com.odontologiaintegralfm.feature.user.dto.UserSecResponseDTO;
import com.odontologiaintegralfm.feature.user.dto.UserSecUpdateDTO;
import com.odontologiaintegralfm.feature.user.model.UserSec;
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


     Response<UserSecResponseDTO> update(UserSecUpdateDTO userSecUpdateDTO);


     void incrementFailedAttempts(String username);

     boolean verifyAttempts(String username);

     UserSec blockAccount(String username);

     void enableAccount(String username);

     void resetFailedAttempts (String username);
}
