package com.odontologiaintegralfm.feature.authentication.service.implement;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ForbiddenException;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.UnauthorizedException;
import com.odontologiaintegralfm.feature.person.core.dto.PersonResponseDTO;
import com.odontologiaintegralfm.feature.person.core.service.implement.PersonService;
import com.odontologiaintegralfm.feature.authentication.dto.*;
import com.odontologiaintegralfm.feature.authentication.model.RefreshToken;
import com.odontologiaintegralfm.feature.authentication.model.Role;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IRoleService;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.feature.user.repository.IUserRepository;
import com.odontologiaintegralfm.feature.user.service.UserService;
import com.odontologiaintegralfm.configuration.securityconfig.core.JwtUtils;
import com.odontologiaintegralfm.infrastructure.message.service.interfaces.IMessageService;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IRefreshTokenService;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.shared.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;


/**
 * Implementación del servicio de autenticación y gestión de usuarios para Spring Security.
 * <p>
 * Esta clase implementa {@link UserDetailsService} y proporciona métodos para:
 * <ul>
 *     <li>Cargar detalles del usuario desde la base de datos.</li>
 *     <li>Autenticar usuarios y validar sus credenciales.</li>
 *     <li>Generar tokens JWT para sesiones autenticadas.</li>
 *     <li>Manejar intentos fallidos de inicio de sesión y bloqueo de cuentas.</li>
 * </ul>
 * </p>
 *
 * <p>
 * La autenticación se realiza mediante {@link #authenticate(String, String)}, que verifica
 * las credenciales del usuario y aplica lógica de seguridad como reintentos y bloqueos.
 * Si la autenticación es exitosa, {@link #loginUser(AuthLoginRequestDTO)} genera un token JWT.
 * </p>
 *
 * <p>
 * Este servicio interactúa con:
 * <ul>
 *     <li>{@link IUserRepository} para obtener los datos del usuario.</li>
 *     <li>{@link JwtUtils} para la generación de tokens JWT.</li>
 *     <li>{@link PasswordEncoder} para la verificación de contraseñas.</li>
 *     <li>{@link IMessageService} para la gestión de mensajes de error.</li>
 *     <li>{@link UserService} para la administración de intentos fallidos y bloqueo de cuentas.</li>
 * </ul>
 * </p>
 *
 * @author [Tu Nombre]
 * @version 1.0
 */
@Slf4j
@Service
public class UserDetailsServiceImp implements UserDetailsService {

    @Autowired
    private IUserRepository userRepo;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IRefreshTokenService refreshTokenService;

    @Autowired
    private PersonService personService;

    @Autowired
    private IRoleService roleService;



    /**
     * Carga un usuario por su nombre de usuario y lo convierte en un objeto {@link UserDetails} de Spring Security.
     * <p>
     * Este método busca el usuario en la base de datos y, si no lo encuentra, lanza una excepción
     * {@link UnauthorizedException}. Luego, obtiene los roles y permisos del usuario,
     * los convierte en una lista de {@link SimpleGrantedAuthority} y devuelve un objeto {@link User}
     * con los datos del usuario y sus permisos.
     * </p>
     *
     * @param username El nombre de usuario del usuario a cargar.
     * @return Un objeto {@link UserDetails} con los datos del usuario y sus permisos.
     * @throws UnauthorizedException Si el usuario no se encuentra en la base de datos.
     */
    @Override
        public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException {
        //Se cuenta con usuario de tipo Usersec y se necesita devolver un tipo UserDetails
        //Se recupera el usuario de la bd
        UserSec userSec = userRepo.findUserEntityByUsername(username)
                .orElseThrow(()-> new UnauthorizedException("exception.usernameNotFound.user", null,"exception.usernameNotFound.log",new Object[]{username,"UserDetailServiceImp", "loadUserByUsername"},LogLevel.WARN));

        //Spring Security maneja permisos con GrantedAuthority
        //Se crea una lista de SimpleGrantedAuthority para almacenar los permisos
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();


        //Se obtiene roles y los convertimos en SimpleGrantedAuthority para poder agregarlos a la authorityList
        for (Role role : userSec.getRolesList()) {

            // Arma el árbol de respuesta entre rol, permisos y acciones.
            RoleFullResponseDTO roleFullResponseDTO = roleService.getFullByRoleId(role.getId());

            // 1. Agregamos el rol como autoridad
            authorityList.add(new SimpleGrantedAuthority("ROLE_" + roleFullResponseDTO.getName()));

            // 2. Por cada permiso y su lista de acciones, agregamos cada combinación como autoridad
            roleFullResponseDTO.getPermissionsList().forEach(permissionDTO -> {
                String permission = permissionDTO.getName().toUpperCase();

                permissionDTO.getActions().forEach(actionDTO -> {
                    String action = actionDTO.getName().toUpperCase();
                    authorityList.add(new SimpleGrantedAuthority("PERMISO_" + permission + "_" + action));
                });
            });
        }

        //Se retorna el usuario en formato Spring Security con los datos del userSec
        return new User(
                userSec.getUsername(),
                userSec.getPassword(),
                userSec.isEnabled(),
                userSec.isAccountNotExpired(),
                userSec.isCredentialNotExpired(),
                userSec.isAccountNotLocked(),
                authorityList
        );
    }



    /**
     * Autentica a un usuario y genera un token JWT si las credenciales son correctas.
     * <p>
     * Este método extrae el nombre de usuario y la contraseña de la solicitud de autenticación,
     * los valida a través del método {@code authenticate}, y si la autenticación es exitosa,
     * almacena la información en el {@link SecurityContextHolder}. Luego, genera un token JWT
     * utilizando {@code jwtUtils.createToken(authentication)} y devuelve una respuesta con los
     * detalles de la autenticación.
     * </p>
     *
     * @param authLoginRequest Un objeto {@link AuthLoginRequestDTO} que contiene las credenciales del usuario.
     * @return Un objeto {@link AuthLoginResponseDTO} con el nombre de usuario, un mensaje de éxito, el token JWT y un estado de autenticación exitoso.
     * @throws UnauthorizedException  Si las credenciales son incorrectas, se lanza una excepción de tipo {@link UnauthorizedException }.
     */

    @LogAction(
            value = "userDetailServiceImpl.systemLogService.login",
            args = {"#result.data.idUser", "#result.data.username"},
            level = LogLevel.INFO,
            type = LogType.SECURITY
    )
    public Response<AuthLoginResponseDTO> loginUser (AuthLoginRequestDTO authLoginRequest){
        try {
            //Se recupera nombre de usuario y contraseña
            String username = authLoginRequest.username();
            String password = authLoginRequest.password();

            // Se invoca al método authenticate.
            Authentication authentication = this.authenticate(username, password);

            //si es autenticado correctamente se almacena la información SecurityContextHolder.
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //Obtiene datos del usuario desde la base de datos.
            UserSec userSec = userService.getByUsername(username);


            //
            PersonResponseDTO personResponseDTO = null;
            if (userSec.getPerson() != null) {
                personResponseDTO = personService.convertToDTO(userSec.getPerson());
            }

            // Elimina el RefreshToken anterior.
            refreshTokenService.deleteRefreshToken(userSec.getId());

            //Crea el JWT
            String accessToken = jwtUtils.createToken(authentication);

            //Crea el RefreshToken
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);

            //Arma el árbol de roles, permisos y acciones.
            Set<RoleFullResponseDTO> roleSet = new HashSet<>();

            for(Role role : userSec.getRolesList()) {
                RoleFullResponseDTO roleFullResponseDTO = roleService.getFullByRoleId(role.getId());
                roleSet.add(roleFullResponseDTO);
            }

            //Construye el DTO para respuesta
            AuthLoginResponseDTO authLoginResponseDTO = AuthLoginResponseDTO.builder()
                    .idUser(userSec.getId())
                    .username(userSec.getUsername())
                    .roles(roleSet)
                    .jwt(accessToken)
                    .refreshToken(refreshToken.getRefreshToken())
                    .person(personResponseDTO)
                    .build();

            return new Response<> (true,"", authLoginResponseDTO);
        }catch (BadCredentialsException ex) {
            throw new UnauthorizedException("exception.badCredentials.user",null, "exception.badCredentials.log",new Object[]{authLoginRequest.username(),"UserDetailServiceImp", "loginUser"}, LogLevel.WARN);
        }
    }



    /**
     * Autentíca a un usuario verificando su nombre de usuario y contraseña.
     * <p>
     * Este método recupera los detalles del usuario a partir del nombre de usuario proporcionado,
     * valida la contraseña ingresada contra la almacenada en la base de datos y maneja intentos
     * fallidos, bloqueo de cuenta y reactivación si es necesario. Si la autenticación es exitosa,
     * retorna un objeto {@link UsernamePasswordAuthenticationToken}.
     * </p>
     *
     * @param username Nombre de usuario del usuario que intenta autenticarse.
     * @param password Contraseña proporcionada por el usuario.
     * @return Un objeto {@link Authentication} que representa la autenticación del usuario si las credenciales son correctas.
     * @throws UnauthorizedException Si el usuario no es encontrado en la base de datos o Si la contraseña es incorrecta.
     * @throws ForbiddenException Si la cuenta ha sido bloqueada debido a intentos fallidos de inicio de sesión.
     */
    public Authentication authenticate (String username, String password) {
        //Se recupera información de todos los detalles del usuario por el username
        UserDetails userDetails = this.loadUserByUsername(username);

        // En caso que sea nulo, se informa que no se pudo encontrar al usuario.
        if (userDetails == null) {
            String logMessage = messageService.getMessage("exception.UsernameNotFound.log", new Object[]{username}, LocaleContextHolder.getLocale());
            throw new UnauthorizedException("exception.usernameNotFound.user", null,"exception.usernameNotFound.log",new Object[]{username,"UserDetailServiceImp", "authenticate"}, LogLevel.WARN);
        }

        //En caso que no coincidan las credenciales se informa que la password es incorrecta
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {

            //Se incrementa intentos fallidos.
            userService.incrementFailedAttempts(username);

            //Verifica intentos de inicio de sesión.
            boolean status = userService.verifyAttempts(username);

            //Se bloquea en caso de igualar o exceder el limite.
            if(!status){
                UserSec userSec = userService.blockAccount(username);
                throw new ForbiddenException("exception.blockAccount.user",null,"exception.blockAccount.log",new Object[]{userSec.getId(), userSec.getUsername(), "UserDetailServiceImp", "authenticate"},LogLevel.WARN);
            }
            throw new UnauthorizedException("exception.badCredentials.user",null, "exception.badCredentials.log", new Object[]{username,"UserDetailServiceImp", "authenticate"},LogLevel.WARN);
        }

        //Verifica si está activa la cuenta.
        userService.enableAccount(username);

        //Resetea intentos fallidos a 0.
        userService.resetFailedAttempts(username);


        return new UsernamePasswordAuthenticationToken(username, userDetails.getPassword(), userDetails.getAuthorities());
    }

    /**
     * Actualiza el refresh token y emite un nuevo JWT.
     *
     * <p>Este método obtiene el token de refresco actual de la base de datos por el ID de usuario,
     * lo valida, elimina el refresh token antiguo, genera uno nuevo y actualiza el JWT
     * utilizado para la autenticación. Finalmente, devuelve una respuesta que contiene el
     * nuevo token de refresco y el JWT junto con los detalles del usuario.</p>
     *
     * @param refreshTokenRequestDTO El objeto de transferencia de datos que contiene el ID del usuario,
     *                        el nombre de usuario y el token de refresco a actualizar.
     *
     * @return Un objeto {@link Response} que contiene el estado de éxito, un mensaje,
     *         y un objeto {@link RefreshTokenRequestDTO} con el nuevo token de refresco y el JWT.
     */
    public Response<RefreshTokenResponseDTO> refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        //Obtiene el refresh token desde la base de datos por Id usuario.
        RefreshToken refreshToken = refreshTokenService.getRefreshTokenByUserId(refreshTokenRequestDTO.getIdUser());

        // Valída el código y la expiración.
        refreshTokenService.validateRefreshToken(refreshToken, refreshTokenRequestDTO);

        //Elimina el refresh Token actual.
        refreshTokenService.deleteRefreshToken(refreshTokenRequestDTO.getRefreshToken());

        //Generar un nuevo refresh Token y guarda en la base.
        RefreshToken refreshTokenNew =  refreshTokenService.createRefreshToken(refreshTokenRequestDTO.getUsername());

        // Obtiene datos del usuario para generar el objeto userDetails.
        UserDetails userDetails = this.loadUserByUsername(refreshTokenRequestDTO.getUsername());

        // Crea un objeto authentication.
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        //Genera un nuevo JWT.
        String jwt = jwtUtils.createToken(authentication);

        //Actualiza valores en el objeto respuesta.
        RefreshTokenResponseDTO refreshTokenResponse = new RefreshTokenResponseDTO();
        refreshTokenResponse.setRefreshToken(refreshTokenNew.getRefreshToken());
        refreshTokenResponse.setJwt(jwt);
        refreshTokenResponse.setIdUser(refreshTokenRequestDTO.getIdUser());
        refreshTokenResponse.setUsername(refreshTokenRequestDTO.getUsername());

        //Descifra la clave del mensaje.
        String message = messageService.getMessage("userDetailServiceImpl.refreshToken.ok", null, LocaleContextHolder.getLocale());
        return  new Response<>(true,message ,refreshTokenResponse);
    }



    /**
     * Cierra la sesión del usuario eliminando el refresh token.
     *
     * Este método elimina el refresh token asociado al usuario, invalidando cualquier futuro intento de
     * renovación del token. Después de la eliminación, se genera un mensaje de éxito que es enviado
     * en la respuesta.
     *
     * @param refreshTokenRequestDTO El objeto que contiene el refresh token a eliminar. Debe incluir
     *                        el refresh token para identificar al usuario.
     *
     * @return Un objeto {@link Response} con el siguiente contenido:
     *         <ul>
     *             <li><b>success</b>: Indica si la operación fue exitosa (true).</li>
     *             <li><b>message</b>: Un mensaje que indica el éxito de la operación.</li>
     *             <li><b>data</b>: null (ya que no se devuelve ningún dato adicional).</li>
     *         </ul>
     */

    @LogAction(
            value = "userDetailServiceImpl.systemLogService.logout",
            args = {"#refreshTokenRequestDTO.idUser"},
            type = LogType.SECURITY,
            level = LogLevel.INFO
    )
    public Response<String> logout(RefreshTokenRequestDTO refreshTokenRequestDTO){

        //Elimina el refresh token del usuario
        refreshTokenService.deleteRefreshToken(refreshTokenRequestDTO.getRefreshToken());

        //Descifra la clave del mensaje
        String message = messageService.getMessage("userDetailServiceImpl.logout.ok", null, LocaleContextHolder.getLocale());

        return new Response<>(true,message ,null);
    }

}
