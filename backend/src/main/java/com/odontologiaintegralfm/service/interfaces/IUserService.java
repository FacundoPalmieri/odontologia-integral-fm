package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.model.UserSec;
import jakarta.servlet.http.HttpServletRequest;

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
     Response<List<UserSecResponseDTO>> findAll();

     /**
      * Obtiene un usuario por su ID.
      * @param id El ID del usuario a recuperar.
      * @return Una respuesta que contiene el objeto {@link UserSecResponseDTO} correspondiente al usuario.
      */
     Response<UserSecResponseDTO> findById(Long id);



     /**
      * Obtiene un usuario por su username.
      * @param username El username del usuario a recuperar.
      * @return el objeto {@link UserSec} correspondiente al usuario.
      */
     UserSec getByUsername(String username);


     /**
      * Guarda un nuevo usuario.
      * @param userSecCreateDto El DTO con la información del usuario a guardar.
      * @return Una respuesta que contiene el objeto {@link UserSecResponseDTO} del usuario recién creado.
      */
     Response<UserSecResponseDTO> save(UserSecCreateDTO userSecCreateDto);


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
