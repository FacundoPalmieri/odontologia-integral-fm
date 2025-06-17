package com.odontologiaintegralfm.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.*;
import com.odontologiaintegralfm.model.*;
import com.odontologiaintegralfm.repository.IUserRepository;
import com.odontologiaintegralfm.service.interfaces.IEmailService;
import com.odontologiaintegralfm.service.interfaces.IFailedLoginAttemptsService;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import com.odontologiaintegralfm.service.interfaces.IUserService;
import com.odontologiaintegralfm.configuration.securityConfig.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio encargado de gestionar las operaciones relacionadas con los usuarios del sistema, incluyendo
 * la creación, actualización y eliminación de usuarios. Este servicio también se encarga de la gestión de
 * contraseñas, habilitación y desbloqueo de cuentas, así como de los intentos fallidos de inicio de sesión.
 * Además, maneja las validaciones de roles, la actualización de datos de los usuarios y el envío de correos
 * electrónicos relacionados con la administración de cuentas de usuario.

 * Métodos disponibles:
 * <ul>
 *   <li>{@link IUserService#getAll()}: Recupera la lista de todos los usuarios del sistema.</li>
 *   <li>{@link IUserService#getById(Long)}: Busca un usuario por su identificador único.</li>
 *   <li>{@link IUserService#create(UserSecCreateDTO)}: Guarda un nuevo usuario en la base de datos.</li>
 *   <li>{@link UserService#update(UserSecUpdateDTO)}: Actualiza la información de un usuario existente.</li>
 *   <li>{@link UserService#encriptPassword(String)}: Encripta una contraseña utilizando el algoritmo BCrypt.</li>
 *   <li>{@link UserService#createTokenResetPasswordForUser(String)}: Crea un token de restablecimiento de contraseña y envía un correo electrónico.</li>
 *   <li>{@link UserService#updatePassword(ResetPasswordRequestDTO, HttpServletRequest)}: Actualiza la contraseña de un usuario utilizando un token de restablecimiento válido.</li>
 *   <li>{@link #unlockAccount(UserSec)}</li>
 *   <li>{@link #incrementFailedAttempts(String)}</li>
 *   <li>{@link #resetFailedAttempts(String)}</li>
 *   <li>{@link #enableAccount(String)}</li>
 *   <li>{@link #validateNotDevRole(UserSecCreateDTO)}</li>
 *   <li>{@link #validateSelfUpdate(Long)}</li>
 *   <li>{@link #validateNotDevRole(UserSec, UserSecUpdateDTO)}</li>
 *   <li>{@link #validateUpdate(UserSec, UserSecUpdateDTO, Set)}</li>
 *   <li>{@link #updateUserSec(UserSec, UserSecUpdateDTO)}</li>
 * </ul>
 *
 * <p>
 * Este servicio interactúa con la base de datos a través del repositorio {@link IUserRepository}, gestiona mensajes
 * y notificaciones a través de {@link IMessageService}, y utiliza {@link JwtUtils} para la creación de tokens
 * de autenticación. Además, se integra con {@link IEmailService} para el envío de correos electrónicos y
 * {@link RoleService} para la asignación de roles a los usuarios.
 * </p>
 */

@Slf4j
@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IFailedLoginAttemptsService failedLoginAttemptsService;

    @Autowired
    private DentistService dentistService;

    @Autowired
    private PersonService personService;

    @Autowired
    @Lazy
    private AuthenticatedUserService authenticatedUserService;


    /**
     * Recupera la lista de todos los usuarios del sistema.
     * <p>
     * Este método obtiene todos los usuarios desde el repositorio {@link IUserRepository},
     * los convierte a objetos {@link UserSecResponseDTO} y los retorna dentro de un objeto {@link Response}.
     * </p>
     * <p>
     * Si ocurre un error al acceder a la base de datos, lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @return Un objeto {@link Response} que contiene una lista de {@link UserSecResponseDTO} con los datos de los usuarios.
     * @throws DataBaseException Si ocurre un error en la consulta a la base de datos.
     */
    @Override
    public Response<Page<UserSecResponseDTO>> getAll(int page, int size, String sortBy, String direction) {
        try{

            //Define criterio de ordenamiento
            Sort sort = direction.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            //Se define paginación con n°página, cantidad elementos y ordenamiento.
            Pageable pageable = PageRequest.of(page,size, sort);

            //Obtiene listado de usuarios.
            Page<UserSec> userList = userRepository.findAll(pageable);


            Page<UserSecResponseDTO> userSecResponseDTOList = userList
                    .map(user -> {

                            PersonResponseDTO personDTO = null;
                            DentistResponseDTO dentistDTO = null;

                            if (user.getPerson() != null) {
                                personDTO = personService.convertToDTO(personService.getById(user.getPerson().getId()));
                                dentistDTO = dentistService.convertToDTO(dentistService.getById(user.getPerson().getId()));
                            }

                            return new UserSecResponseDTO(
                                    user.getId(),
                                    user.getUsername(),
                                    user.getRolesList(),
                                    user.isEnabled(),
                                    personDTO,
                                    dentistDTO
                            );
            });


            String messageUser = messageService.getMessage("userService.getAll.ok", null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser, userSecResponseDTOList);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", 0L, "", "getAll");
        }
    }




    /**
     * Busca un usuario en el sistema por su identificador único.
     * <p>
     * Este método consulta la base de datos para obtener un usuario con el ID proporcionado.
     * Si el usuario es encontrado, se convierte en un objeto {@link UserSecResponseDTO} y se devuelve dentro
     * de un objeto {@link Response}. Si no se encuentra, se lanza una excepción {@link NotFoundException}.
     * </p>
     * <p>
     * En caso de error en la consulta a la base de datos, se lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param id Identificador único del usuario a buscar.
     * @return Un objeto {@link Response} que contiene los datos del usuario encontrado.
     * @throws NotFoundException Si no se encuentra un usuario con el ID proporcionado.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public Response<UserSecResponseDTO> getById(Long id) {
        try{
             Optional<UserSec> user = userRepository.findById(id);
             if(user.isPresent()){
                 UserSecResponseDTO dto = convertToDTO(user.get());

                 String messageUser = messageService.getMessage("userService.getById.ok.user", null, LocaleContextHolder.getLocale());

                 return new Response<>(true, messageUser, dto);
             }else{
                 throw new NotFoundException("userService.findById.error.user", null,"userService.findById.error.log",new Object[]{id,"UserService", "getById"}, LogLevel.ERROR );
             }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", id, "", "getById");
        }
    }

    @Override
    public UserSec getByUsername(String username) {
        try {
            return userRepository.findUserEntityByUsername(username).orElseThrow(() -> new NotFoundException("userService.getByUsername.error.user", null, "userService.getByUsername.error.log",new Object[]{username,"UserService", "getByUsername"}, LogLevel.ERROR ));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", 0L, "", "getByUsername");
        }
    }





    /**
     * Guarda un nuevo usuario en la base de datos junto con su información personal y, si corresponde, como dentista.
     * <p>
     * Este método realiza varias validaciones antes de persistir el usuario:
     * <ul>
     *     <li>Verifica que el nombre de usuario no exista previamente en la base de datos.</li>
     *     <li>Valida que no se asigne un rol de desarrollador (DEV) al nuevo usuario.</li>
     *     <li>Valida que las contraseñas ingresadas coincidan.</li>
     *     <li>Construye el objeto {@link UserSec} a partir del DTO recibido y lo persiste.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Posteriormente, se realiza la creación de una entidad {@link Person} asociada al usuario:
     * <ul>
     *     <li>La persona es obligatoria y se crea mediante el {@link PersonService}.</li>
     *     <li>La persona puede representar a una secretaria o un dentista, dependiendo del DTO recibido.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Si se incluye un {@link DentistCreateRequestDTO}, se crea también un {@link Dentist} asociado a la persona.
     * </p>
     *
     * <p>
     * Si la operación es exitosa, se devuelve un {@link Response} conteniendo el usuario guardado, junto con los datos de persona y dentista (si aplica), en formato {@link UserSecResponseDTO}.
     * En caso de error en la transacción, se lanza una {@link DataBaseException}.
     * Si no se proporciona información de persona, se lanza una {@link ConflictException}.
     * </p>
     *
     * @param userSecCreateDto Objeto {@link UserSecCreateDTO} con la información del usuario, persona y dentista (opcional) a guardar.
     * @return Un {@link Response} con los datos del usuario guardado, incluyendo la persona y el dentista si corresponde.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
     * @throws ConflictException Si no se puede crear la persona asociada al usuario.
     */

    @Override
    @Transactional
    public Response<UserSecResponseDTO> create(UserSecCreateDTO userSecCreateDto) {
        try{
            //Valída que el usuario no exista en la base de datos.
            validateUsername(userSecCreateDto.getUsername());

            //Valida que no se asigne un rol DEV al nuevo usuario.
            validateNotDevRole(userSecCreateDto);

            //Valída que las pass sean coincidentes.
            validatePasswords(userSecCreateDto.getPassword1(), userSecCreateDto.getPassword2(), userSecCreateDto.getUsername());

            //Construye la entidad para persistirla.
            UserSec userSec = buildUserSec(userSecCreateDto);

            //Guarda el usuario en la base de datos.
            UserSec userSecSaved = userRepository.save(userSec);

            //Setea valores del UserSec persistido en la respuesta.
            UserSecResponseDTO userSecResponse = new UserSecResponseDTO();
            userSecResponse.setId(userSecSaved.getId());
            userSecResponse.setUsername(userSecSaved.getUsername());
            userSecResponse.setRolesList(userSecSaved.getRolesList());
            userSecResponse.setEnabled(userSecSaved.isEnabled());

            //Se construye Mensaje para usuario.
            String userMessage = messageService.getMessage("userService.save.ok", null, LocaleContextHolder.getLocale());

            Person person;

            //Creación de la Persona (Puede ser Secretaría o Dentista)
            if(userSecCreateDto.getPerson() != null) {
                person = personService.create(userSecCreateDto.getPerson());
                PersonResponseDTO personResponseDTO = personService.convertToDTO(person);
                userSec.setPerson(person);

                //Se agrega PersonaDTO a la respuesta final
                userSecResponse.setPerson(personResponseDTO);

                //Creación de Dentista
                if(userSecCreateDto.getDentist() != null) {
                    Dentist dentist = dentistService.create(person,userSecCreateDto.getDentist());
                    DentistResponseDTO dentistResponseDTO = dentistService.convertToDTO(dentist);

                    //Se agrega DentistaDTO  a la respuesta final
                    userSecResponse.setDentist(dentistResponseDTO);
                }
            }

            return new Response<>(true, userMessage,userSecResponse);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", null, userSecCreateDto.getUsername(), "create");
        }
    }




    /**
     * Actualiza la información de un usuario en la base de datos.
     * <p>
     * Este método realiza varias validaciones antes de actualizar los datos del usuario,
     * incluyendo restricciones sobre la actualización de usuarios con rol "DEV" y cambios
     * en el estado de la cuenta o los roles. Finalmente, guarda los cambios en la base de datos.
     * </p>
     *
     * @param userSecUpdateDto DTO que contiene los datos actualizados del usuario.
     * @return {@link Response} que contiene el {@link UserSecResponseDTO} con los datos actualizados.
     * @throws NotFoundException  Si el usuario a actualizar no se encuentra en la base de datos.
     * @throws ConflictException  Si ocurre un error durante la actualización.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos.
     */
    @Transactional
    @Override
    public Response<UserSecResponseDTO> update(UserSecUpdateDTO userSecUpdateDto) {
        try {
            //Se obtiene el usuario desde la base de datos para realizar validaciones
            UserSec userSec = userRepository.findById(userSecUpdateDto.getId())
                    .orElseThrow(() -> new NotFoundException("userService.findById.error.user", null,"userService.findById.error.log",new Object[]{userSecUpdateDto.getId(), "UserService", "Update"},LogLevel.ERROR));

            //Valída que el ID del UserSecUpdate no sea el mismo de quien está autenticado y recibe el usuario de la BD.
            validateSelfUpdate(userSecUpdateDto.getId());

            //Valída que el ID del userSecUpdate no sea posea un rol DEV o que el usuario a actualizar no sea un usuario DEV
            validateNotDevRole(userSec, userSecUpdateDto);

            //Valída cambio que haya al menos una actualización de datos.
            validateUpdate(userSec, userSecUpdateDto, userSec.getRolesList());

            //Actualiza datos en el UserSec
            UserSec userSecAux = updateUserSec(userSec, userSecUpdateDto);

            //Guarda en base de datos.
            UserSec userSaved = userRepository.save(userSecAux);

            //Convierte la entidad a un DTO.
            UserSecResponseDTO userSecResponse = convertToDTO(userSaved);

            //Se construye Mensaje para usuario.
            String userMessage = messageService.getMessage("userService.update.ok", null, LocaleContextHolder.getLocale());

            //Actualizar datos de la Persona.
            if(userSecUpdateDto.getPerson() != null) {
                Person person = personService.getById(userSecUpdateDto.getId());
                person = personService.update(person,userSecUpdateDto.getPerson());

                userSec.setPerson(person);
                PersonResponseDTO personResponseDTO = personService.convertToDTO(person);

                //Se agrega PersonaDTO a la respuesta final
                userSecResponse.setPerson(personResponseDTO);

            }

            // Verifica si se actualizan datos de Dentista.
            if(userSecUpdateDto.getDentist() != null) {
                Dentist dentist = dentistService.getById(userSecUpdateDto.getId());
                dentist = dentistService.update(dentist,userSecUpdateDto.getDentist());

                DentistResponseDTO dentistResponseDTO = dentistService.convertToDTO(dentist);

                //Se agrega DentistResponseDTO a la respuesta final
                userSecResponse.setDentist(dentistResponseDTO);
            }

            return new Response<>(true, userMessage, userSecResponse);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", userSecUpdateDto.getId(), "", "Update");
        }
    }


    /**
     * Encripta una contraseña utilizando el algoritmo BCrypt.
     * <p>
     * Este método recibe una contraseña en texto plano y la encripta utilizando el algoritmo BCrypt,
     * que es una técnica común para el almacenamiento seguro de contraseñas.
     * </p>
     *
     * @param password La contraseña en texto plano que se desea encriptar.
     * @return La contraseña encriptada utilizando el algoritmo BCrypt.
     */
    @Override
    public String encriptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }



    /**
     * Crea un token de restablecimiento de contraseña para un usuario y envía un correo electrónico con un enlace de restablecimiento.
     * <p>
     * Este método genera un token JWT para restablecer la contraseña de un usuario basado en su correo electrónico (nombre de usuario).
     * Si el usuario existe, el token se asigna a la propiedad `resetPasswordToken` del usuario,
     * y se envía un correo electrónico con un enlace para restablecer la contraseña.
     * </p>
     *
     * @param email El correo electrónico (nombre de usuario) del usuario que solicita el restablecimiento de contraseña.
     * @return Un mensaje indicando si el proceso de creación del token fue exitoso.
     * @throws UnauthorizedException  Si no se encuentra un usuario con el correo electrónico proporcionado.
     * @throws DataBaseException Si ocurre un error en la base de datos durante el proceso.
     */
    @Override
    @Transactional
    public Response<String> createTokenResetPasswordForUser(String email) {
        try {
            Optional<UserSec> userOptional = userRepository.findUserEntityByUsername(email);
            if (userOptional.isEmpty()) {
                throw new UnauthorizedException("exception.usernameNotFound.user",null,"exception.usernameNotFound.log",new Object[]{email,"UserService", "validateToken"},LogLevel.ERROR);
            }

            UserSec user = userOptional.get();

            // Crear los authorities manualmente desde el usuario
            List<GrantedAuthority> authorities = user.getRolesList().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getRole()))
                    .collect(Collectors.toList());


            // Crear autenticación usando el username y los authorities
            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

            // Generar el token
            String token = jwtUtils.createToken(authentication);

            // Asignar el token al usuario
            user.setResetPasswordToken(token);
            userRepository.save(user);

            // Obtener la URL base desde el archivo properties.
            String dominio = messageService.getMessage("userService.dominio", null, LocaleContextHolder.getLocale());

            // Construir la URL de restablecimiento de contraseña
            String resetUrl = dominio + "?token=" + token;

            // Obtener el mensaje completo con la URL de restablecimiento
            String message = messageService.getMessage("userService.requestResetPassword.mensaje", new Object[] {resetUrl}, LocaleContextHolder.getLocale());

            //Asunto del email
            String asunto = messageService.getMessage("userService.requestResetPassword.asunto", null, LocaleContextHolder.getLocale());

            //Envío de email
            emailService.sendEmail(user.getUsername(), asunto, message);

            //Elaborar respuesta para el controller.
            String messageUser =  messageService.getMessage("userService.requestResetPassword.success", null, LocaleContextHolder.getLocale());

            return new Response <>(true, messageUser, user.getUsername());

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", 0L, "", "createPasswordReset");
        }
    }




    /**
     * Actualiza la contraseña de un usuario utilizando un token de restablecimiento de contraseña válido.
     * <p>
     * Este método valida el token de restablecimiento de contraseña, verifica que las nuevas contraseñas coincidan,
     * desbloquea la cuenta si estaba bloqueada, encripta y guarda la nueva contraseña en la base de datos,
     * y notifica al usuario mediante un correo electrónico. También registra la acción en los logs del sistema.
     * </p>
     *
     * @param resetPasswordRequestDTO Objeto que contiene el token de restablecimiento, la nueva contraseña y su confirmación.
     * @param request Objeto {@link HttpServletRequest} para obtener la dirección IP del usuario que realiza la solicitud.
     * @return Un mensaje indicando si el restablecimiento de la contraseña fue exitoso.
     * @throws DataBaseException Si ocurre un error en la base de datos durante el proceso.
     */
    @Override
    @Transactional
    public Response<String> updatePassword(ResetPasswordRequestDTO resetPasswordRequestDTO, HttpServletRequest request) {
        try {
            //Valída Token de restablecimiento.
            validateTokenResetPassword(resetPasswordRequestDTO.token());

            //Obtiene información del usuario.
            UserSec usuario = userRepository.findByResetPasswordToken(resetPasswordRequestDTO.token());

            //Valída que coincidan las passwords.
            validatePasswords(resetPasswordRequestDTO.newPassword1(), resetPasswordRequestDTO.newPassword2(),usuario.getUsername());

            //Desbloquea la cuenta.
            unlockAccount(usuario);

            //Encripa la password
            String passwordEncrypted = encriptPassword(resetPasswordRequestDTO.newPassword1());
            usuario.setPassword(passwordEncrypted);
            usuario.setResetPasswordToken(null);
            userRepository.save(usuario);

            //Envía correo al usuario.
            String message = messageService.getMessage("userService.resetPassword.success", null, LocaleContextHolder.getLocale());
            String asunto = messageService.getMessage("userService.resetPassword.asunto", null, LocaleContextHolder.getLocale());
            emailService.sendEmail(usuario.getUsername(), asunto, message);

            //Obtener dirección IP
            String ipAddress = request.getRemoteAddr();

            //Guardado de Log
            log.atInfo().log("[Mensaje: {}] - [USUARIO: {}] -[IP {}]", message,usuario.getUsername(), ipAddress);

            //Elabora Response a controller.
            String messageUser = messageService.getMessage("userService.resetPassword.success", null, LocaleContextHolder.getLocale());

            return new Response <>(true, messageUser, usuario.getUsername());

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", 0L, "", "updatePassword");
        }
    }



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
    protected boolean verifyAttempts(String username){
        try{
            int configAttempts = failedLoginAttemptsService.get();
            int userAttempts = userRepository.findFailedLoginAttemptsByUsername(username);
            if(userAttempts >= configAttempts){
                return false;
            }
            return true;
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", 0L, username, "verifyAttempts");
        }
    }



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
    @Transactional
    protected UserSec blockAccount(String username){
        Long userId = 0L; // Variable para almacenar el ID del usuario
        try{
            Optional<UserSec>userSec = userRepository.findUserEntityByUsername(username);
            if(userSec.isEmpty()){
                throw new UnauthorizedException("exception.usernameNotFound.user", null, "exception.usernameNotFound.log",new Object[]{username,"UserService", "blockAccount"}, LogLevel.ERROR);
            }

            //Obtener id Usuario para exception
            userId = userSec.get().getId();

            UserSec user = userSec.get();
            user.setAccountNotLocked(false);
            user.setLocktime(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService",userId, username, "blockAccount");
        }
    }



    /**
     * Desbloquea la cuenta de un usuario, restableciendo los intentos fallidos de inicio de sesión.
     * <p>
     * Este método establece la cuenta del usuario como desbloqueada, reinicia el contador de intentos fallidos,
     * elimina la marca de tiempo de bloqueo y actualiza la fecha de última modificación antes de guardar los cambios en la base de datos.
     * </p>
     *
     * @param user El objeto {@link UserSec} que representa al usuario cuya cuenta será desbloqueada.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
     */
    @Transactional
    protected void unlockAccount(UserSec user){
        try{
            user.setAccountNotLocked(true);
            user.setFailedLoginAttempts(0);
            user.setLocktime(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService",user.getId(), user.getUsername(), "blockAccount");
        }
    }



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
    @Transactional
    protected void incrementFailedAttempts(String username) {
        Optional<UserSec> userSec = userRepository.findUserEntityByUsername(username);

        if(userSec.isPresent()) {
            UserSec userSecOK = userSec.get();
            userSecOK.setFailedLoginAttempts(userSecOK.getFailedLoginAttempts() + 1);
            userSecOK.setUpdatedAt(LocalDateTime.now());
            try {
                userRepository.save(userSecOK);
            }catch (DataAccessException | CannotCreateTransactionException e) {
                throw new DataBaseException(e, "userService", userSecOK.getId(), username, "incrementFailedAttempts");
            }
        }
    }


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
    @Transactional
    protected void resetFailedAttempts(String username) {
        Optional<UserSec> userSec = userRepository.findUserEntityByUsername(username);
        if(userSec.isPresent()) {
            UserSec userSecOK = userSec.get();
            userSecOK.setFailedLoginAttempts(0);
            userSecOK.setUpdatedAt(LocalDateTime.now());
            try{
                userRepository.save(userSecOK);
            }catch (DataAccessException | CannotCreateTransactionException e) {
                throw new DataBaseException(e, "userService", userSecOK.getId(), username, "decrementFailedAttempts");
            }
        }
    }



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
    protected void enableAccount(String username){
        Long id = 0L;
        try{
            Optional<UserSec> userSec = userRepository.findUserEntityByUsername(username);
            if(userSec.isPresent()) {
                UserSec userSecOK = userSec.get();
                id = userSecOK.getId();
                if(!userSecOK.isEnabled()) {
                    throw new UnauthorizedException("exception.usernameNotFound.user", null, "exception.usernameNotFound.log",new Object[]{username,"UserService", "enableAccount"}, LogLevel.ERROR);
                }
            }
        }catch(DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", id, username, "enableAccount");
        }

    }




    /**
     * Valída que no se pueda crear un nuevo usuario con el rol "DEV".
     * <p>
     * Este método recorre la lista de roles del usuario y verifica si alguno de ellos
     * corresponde al rol "DEV". Si es así, se lanza una {@link ConflictException}.
     * </p>
     *
     * @param userSecCreateDto DTO que contiene la lista de roles del usuario.
     * @throws ConflictException Si se intenta asignar el rol "DEV" al usuario.
     */
    private void validateNotDevRole(UserSecCreateDTO userSecCreateDto) {
        //Obtener los roles mediante el ID del DTO
        for (Long id : userSecCreateDto.getRolesList()) {
            Role role = roleService.getByIdInternal(id);

            //Valída que la creación no sea a un rol DEV
            if (role.getRole().equals("Dev") || role.getRole().equals("DEV")) {
                throw new ConflictException("exception.save.validateNotDevRole.user", null,"exception.save.validateNotDevRole.log",new Object[]{role.getId(),"UserService", "validateNotDevRole"},LogLevel.ERROR);
            }
        }
    }

    /**
     * Valida que el usuario autenticado no intente actualizar sus propios datos.
     * <p>
     * Este método compara el ID del usuario autenticado con el ID proporcionado en la solicitud de actualización.
     * Si ambos IDs coinciden, se lanza una excepción {@link ConflictException}.
     * </p>
     *
     * @param id ID del usuario cuyo dato se está intentando actualizar.
     * @throws NotFoundException  Si no se encuentra al usuario autenticado en la base de datos.
     * @throws ConflictException Si el usuario autenticado intenta actualizar sus propios datos.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos.
     */
    private void validateSelfUpdate(Long id) {
       try {
           //Obtener el username del usuario autenticado
           Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           String authenticatedUsername;

           if (principal instanceof UserDetails) {
               authenticatedUsername = ((UserDetails) principal).getUsername();
           } else {
               authenticatedUsername = principal.toString();
           }

           //Obtener el ID del usuario autenticado.
           UserSec userSec = userRepository.findUserEntityByUsername(authenticatedUsername).orElseThrow(() -> new NotFoundException("userService.findById.error.user",null,"userService.findById.error.log",new Object[]{authenticatedUsername,"UserService", "validateSelfUpdate"},LogLevel.ERROR));

           //Comparar el ID del usuario autenticado con el ID de la solicitud.
           if (userSec.getId().equals(id)) {
               throw new ConflictException("exception.validateSelfUpdate.user",null,"exception.validateSelfUpdate.log",new Object[]{id,"UserService", "validateSelfUpdate"},LogLevel.INFO);
           }
       } catch (DataAccessException | CannotCreateTransactionException e) {
           throw new DataBaseException(e, "userService", id, "", "validateSelfUpdate");
       }
    }


    /**
     * Valida que no se pueda realizar ningún tipo de actualización a un usuario con rol "DEV".
     * <p>
     * Este método primero valida que el usuario que se está actualizando no tenga el rol "DEV" en su lista de roles.
     * Luego, revisa si los roles que se están intentando asignar al usuario incluyen el rol "DEV". Si alguno de estos roles
     * es "DEV", se lanza una excepción {@link ConflictException}.
     * </p>
     *
     * @param userSec Objeto que representa el usuario actual con sus roles.
     * @param userSecUpdateDto DTO que contiene los roles que se quieren asignar al usuario.
     * @throws ConflictException  Si el usuario tiene el rol "DEV" o se intenta asignar el rol "DEV".
     */
    private void validateNotDevRole(UserSec userSec, UserSecUpdateDTO userSecUpdateDto) {
        //Valída que no pueda realizar ningún tipo de actualización a un usuario de tipo DEV
        for (Role role : userSec.getRolesList()) {
            if (role.getRole().equals("Dev") || role.getRole().equals("DEV")) {
                throw new ConflictException("exception.update.validateNotDevRole.user",null,"exception.update.validateNotDevRole.log",new Object[]{ role.getId(),"UserService", "validateNotDevRole"},LogLevel.INFO);
            }
        }

        //Si la lista está vacía no es necesario continuar con las validaciones.
        if (userSecUpdateDto.getRolesList().isEmpty()) {
            return;
        }


        //Obtener los roles mediante el ID del DTO
        for (Long id : userSecUpdateDto.getRolesList()) {
            Role role = roleService.getByIdInternal(id);

            //Valída que la actualización no sea a un rol DEV
            if (role.getRole().equals("Dev") || role.getRole().equals("DEV")) {
                throw new ConflictException("exception.update.validateNotDevRole.user",null,"exception.update.validateNotDevRole.log",new Object[]{ role.getId(),"UserService", "validateNotDevRole"},LogLevel.INFO);
            }
        }
    }


    /**
     * Valída que no se intente realizar una actualización que no cambie el estado de la cuenta del usuario y la lista de roles.
     * <p>
     * Este método compara el estado de la cuenta actual del usuario (habilitado o deshabilitado) con el estado que se quiere
     * asignar en el DTO de actualización. A su vez, compara la lista de roles actuales con la que recibe mediante el DTO. Si no hay modificaciones, se lanza una excepción {@link ConflictException}.
     * </p>
     *
     * @param userSec Objeto que representa al usuario con su estado actual de cuenta.
     * @param userSecUpdateDto DTO que contiene el nuevo estado de la cuenta.
     * @param roleList Lista de roles del usuario obtenida desde la base de datos.
     * @throws ConflictException  Si el estado de la cuenta no cambia (es igual al actual).
     */
    private void validateUpdate (UserSec userSec, UserSecUpdateDTO userSecUpdateDto,Set<Role>roleList){
        boolean validateAccount = false;
        boolean validateRole = false;

        if (userSecUpdateDto.getEnabled() == null) {
            return;
        }


        if (userSec.isEnabled() == userSecUpdateDto.getEnabled()) {
            validateAccount = true;
        }

        if(userSecUpdateDto.getRolesList() == null){
            return;
        }

        //Se extraen los IDs de tipo Long del Set<Role>roleList
        Set<Long> roleListId = new HashSet<>();
        for(Role role : roleList){
            roleListId.add(role.getId());
        }

        //Se compara los IDs en los set <Long>
        if(userSecUpdateDto.getRolesList().equals(roleListId)){
            validateRole = true;
        }

        if(validateAccount && validateRole){
            throw new ConflictException("exception.validateUpdateUser.user",null,"exception.validateUpdateUser.log",new Object[]{userSec.getId(),"UserService","validateUpdate"},LogLevel.ERROR);
        }


    }






    /**
     * Actualiza los datos del usuario con la información proporcionada en el DTO de actualización.
     * <p>
     * Este método actualiza el estado de habilitación del usuario y agrega los roles nuevos desde el DTO a la lista de roles
     * del usuario. Si alguno de los roles proporcionados no se encuentra en la base de datos, se lanza una excepción {@link NotFoundException}.
     * </p>
     *
     * @param userSec Objeto que representa al usuario a ser actualizado.
     * @param userSecUpdateDTO DTO que contiene la información para actualizar al usuario.
     * @return El objeto {@link UserSec} actualizado con los nuevos valores.
     * @throws NotFoundException Si alguno de los roles proporcionados en el DTO no se encuentra en la base de datos.
     */
    private UserSec updateUserSec(UserSec userSec, UserSecUpdateDTO userSecUpdateDTO){
        if(userSecUpdateDTO.getEnabled() != null){
            userSec.setEnabled(userSecUpdateDTO.getEnabled());
        }

        //Obtener los roles mediante el ID del DTO
        Set<Role> roleList = new HashSet<>();
        for(Long id : userSecUpdateDTO.getRolesList()) {
            Role role = roleService.getByIdInternal(id);
            roleList.add(role);
        }

        if(!userSecUpdateDTO.getRolesList().isEmpty()){
            userSec.getRolesList().clear();
            userSec.getRolesList().addAll(roleList);
        }

        userSec.setUpdatedAt(LocalDateTime.now());
        userSec.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

        return userSec;
    }




    /**
     * Valída que el nombre de usuario no exista en la base de datos.
     * <p>
     * Este método verifica si el nombre de usuario proporcionado ya está registrado en la base de datos.
     * Si el nombre de usuario existe, lanza una {@link ConflictException}.
     * </p>
     *
     * @param username El nombre de usuario a verificar.
     * @throws ConflictException Si el nombre de usuario ya existe en la base de datos.
     */
    private void validateUsername(String username) {
        Optional<UserSec>user = userRepository.findUserEntityByUsername(username);
        if(user.isPresent()){
            UserSec userSec = user.get();
            if(userSec.getUsername().equals(username)) {
                throw new ConflictException("exception.usernameExisting.user",new Object[]{username}, "exception.usernameExisting.log",new Object[]{username,"UserService", "Save"},LogLevel.ERROR);
            }
        }
    }






    /**
     * Valída un token de restablecimiento de contraseña verificando su autenticidad y asociación con un usuario.
     * <p>
     * Este método decodifica el token JWT proporcionado, extrae el nombre de usuario y verifica
     * si coincide con el usuario almacenado en la base de datos que posee ese token de restablecimiento.
     * Si el token es inválido o no está asociado al usuario correcto, lanza una excepción.
     * </p>
     *
     * @param token El token de restablecimiento de contraseña que se desea validar.
     * @throws UnauthorizedException  Si el token no es válido o no pertenece al usuario correcto.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos.
     */
    private void validateTokenResetPassword(String token) {
        try {
            DecodedJWT decodedJWT = jwtUtils.validateToken(token);
            String username = jwtUtils.extractUsername(decodedJWT);
            UserSec usuario = userRepository.findByResetPasswordToken(token);
            if(usuario == null || !usuario.getUsername().equals(username)){
                throw new UnauthorizedException("exception.validateToken.user",null,"exception.validateToken.log",new Object[]{usuario.getUsername(),"UserService", "validateToken"},LogLevel.ERROR);
            }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "userService", 0L, "", "validatePasswordReset");
        }
    }






    /**
     * Valida que las dos contraseñas ingresadas coincidan.
     * <p>
     * Si las contraseñas no son iguales, lanza una excepción de tipo {@code PasswordMismatchException}.
     * </p>
     *
     * @param password1 La primera contraseña ingresada.
     * @param password2 La segunda contraseña ingresada (confirmación).
     * @param username  El nombre de usuario asociado a la operación de cambio de contraseña.
     * @throws ConflictException  Si las contraseñas no coinciden.
     */
    private void validatePasswords(String password1, String password2, String username) {
        if(!password1.equals(password2)){
            throw new ConflictException("exception.passwordNotEquals.user",null,"exception.passwordNotEquals.log", new Object[]{username, "UserService","validatePasswords"},LogLevel.NONE);
        }
    }


    /**
     * Obtiene y valida los roles de un usuario a partir de una lista de roles proporcionada.
     * <p>
     * Recorre la lista de roles y busca cada uno en el servicio de roles. Si un rol no es encontrado,
     * se lanza una excepción de tipo {@code BadRequestException}.
     * </p>
     *
     * @param rolesList Conjunto de roles asociados al usuario.
     * @return Un conjunto de roles válidos obtenidos desde el servicio de roles.
     */
    private Set<Role> getRolesForUser(Set<Long> rolesList) {
        Set<Role> validRoles = new HashSet<>();
        for (Long id : rolesList) {
            Role foundRole = roleService.getByIdInternal(id);
            validRoles.add(foundRole);
        }
        return validRoles;
    }



    /**
     * Convierte una entidad {@link UserSec} a un objeto {@link UserSecResponseDTO}.
     * <p>
     * Este método toma una entidad {@link UserSec} y la convierte en un objeto DTO ({@link UserSecResponseDTO}),
     * copiando sus propiedades principales como el ID, el nombre de usuario y la lista de roles.
     * </p>
     *
     * @param userSec La entidad {@link UserSec} que se va a convertir.
     * @return Un objeto {@link UserSecResponseDTO} que contiene los datos de la entidad {@link UserSec}.
     */
    private UserSecResponseDTO convertToDTO(UserSec userSec) {
        return new UserSecResponseDTO(
                userSec.getId(),
                userSec.getUsername(),
                userSec.getRolesList(),
                userSec.isEnabled(),
                null,
                null
        );
    }



    /**
     * Construye un objeto {@code UserSec} a partir de un DTO {@code UserSecCreateDTO}.
     * <p>
     * Este método utiliza el patrón de diseño builder para crear una nueva instancia de {@code UserSec}
     * a partir de los datos proporcionados en el DTO, configurando valores predeterminados para varios campos
     * como el número de intentos fallidos, el estado de la cuenta y las fechas de creación y última actualización.
     * </p>
     *
     * @param userSecCreateDto El DTO que contiene los datos del usuario.
     * @return Un nuevo objeto {@code UserSec} con los valores especificados en el DTO.
     */
    private UserSec buildUserSec(UserSecCreateDTO userSecCreateDto) {
        return new UserSec(
                userSecCreateDto.getUsername(),
                encriptPassword(userSecCreateDto.getPassword1()),
                0, // failedLoginAttempts
                null, // locktime
                true, // accountNotExpired
                true, // accountNotLocked
                true, // credentialNotExpired
                getRolesForUser(userSecCreateDto.getRolesList()),
                null, // resetPasswordToken
                null, //Person

                // Campos heredados de Auditable
                LocalDateTime.now(), // createdAt
                authenticatedUserService.getAuthenticatedUser(), // createdBy
                null, // updatedAt
                null, // updatedBy
                true, // enabled
                null, // disabledAt
                null  // disabledBy
        );
    }
}

