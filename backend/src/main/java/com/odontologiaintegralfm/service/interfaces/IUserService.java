package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.UserSec;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.util.List;

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
     Response<Page<UserSecResponseDTO>> getAll(int page, int size);

     /**
      * Obtiene un usuario por su ID.
      * @param id El ID del usuario a recuperar.
      * @return Una respuesta que contiene el objeto {@link UserSecResponseDTO} correspondiente al usuario.
      */
     Response<UserSecResponseDTO> getById(Long id);



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

}
