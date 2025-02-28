package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.dto.AuthLoginRequestDTO;
import com.odontologiaintegralfm.dto.AuthResponseDTO;
import com.odontologiaintegralfm.dto.ResetPasswordDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IUserService;
import com.odontologiaintegralfm.service.UserDetailsServiceImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Controlador de autenticación que maneja los endpoints relacionados con el inicio de sesión,
 * la solicitud de restablecimiento de contraseña y el restablecimiento efectivo de la contraseña del usuario.
 *
 * Este controlador expone las siguientes operaciones:
 * <ul>
 *     <li><b>/login</b>: Inicia sesión autenticando las credenciales del usuario y devuelve un token en caso de autenticación exitosa.</li>
 *     <li><b>/request/reset-password</b>: Solicita el restablecimiento de la contraseña enviando un correo electrónico con un enlace de redireccionamiento.</li>
 *     <li><b>/reset-password</b>: Restablece la contraseña del usuario utilizando el token recibido en el correo electrónico.</li>
 * </ul>
 *
 * El controlador depende de los servicios {@link UserDetailsServiceImp} para la autenticación de usuario y
 * {@link IUserService} para la gestión de las contraseñas y el envío de correos electrónicos de restablecimiento.
 */

@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private UserDetailsServiceImp userDetailsService;

    @Autowired
    private IUserService userService;


    /**
     * Inicia sesión autenticando las credenciales del usuario.
     *
     * @param userRequest Objeto que contiene las credenciales del usuario.
     * @return ResponseEntity con:
     *      <ul>
     *          <li><b>200 OK</b>: Autenticado.</li>
     *          <li><b>401 Unauthorized</b>: No autenticado.</li>
     *          <li><b>403 Forbidden</b>: Cuenta bloqueada o sin permisos de acceso.</li>
     *      </ul>
     */
    @Operation(summary = "Autenticación de usuario", description = "Recibe las credenciales del usuario y devuelve un token en caso de autenticación exitosa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado existosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "Cuenta bloqueada o sin permisos de acceso.")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthLoginRequestDTO userRequest) {
        return new ResponseEntity<>(this.userDetailsService.loginUser(userRequest), HttpStatus.OK);
    }


    /**
     *  Solicita el restablecimiento de la contraseña enviando un correo con una URL de redireccionamiento.
     *
     * @param email  Correo electrónico del usuario al cual se enviará el enlace para restablecer la contraseña.
     * @return ResponseEntity con:
     *      <ul>
     *          <li><b>200 OK</b>: Correo enviado exitosamente.</li>
     *      </ul>
     */
    @Operation(summary = "Solicitud de restablecimiento de password", description = "Envía un correo electrónico al usuario con un enlace para restablecer la contraseña.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo enviado exitosamente.")
    })
    @PostMapping("/request/reset-password")
    public ResponseEntity<Response<String>> requestResetPassword(@RequestParam String email) {
        Response<String> response = userService.createTokenResetPasswordForUser(email);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    /**
     * Restablece la contraseña del usuario utilizando el token recibido en el correo electrónico.
     *
     * @param resetPasswordDTO Objeto que contiene el nuevo password del usuario y el token recibido por correo electrónico.
     * @param request Solicitud HTTP que contiene detalles de la petición
     * @return ResponseEntity con:
     *      <ul>
     *          <li><b>200 OK</b>: Restablecimiento de contraseña exitoso.</li>
     *      </ul>
     */
    @Operation(summary = "Restablecimiento de la contraseña.", description = "Restablece la contraseña del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restablecimiento de contraseña exitoso.")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Response<String>> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO, HttpServletRequest request) {
        Response<String> response = userService.updatePassword(resetPasswordDTO, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}





