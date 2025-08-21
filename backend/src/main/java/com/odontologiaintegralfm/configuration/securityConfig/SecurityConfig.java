package com.odontologiaintegralfm.configuration.securityConfig;
import com.odontologiaintegralfm.repository.IUserRepository;
import com.odontologiaintegralfm.configuration.securityConfig.filter.JwtTokenValidator;
import com.odontologiaintegralfm.configuration.securityConfig.filter.OAuth2UserFilter;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import com.odontologiaintegralfm.service.interfaces.IRefreshTokenService;
import com.odontologiaintegralfm.service.interfaces.ISystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IRefreshTokenService refreshTokenService;

    @Autowired
    private ISystemLogService systemLogService;



    /**
     * Configura el filtro de seguridad de la aplicación.
     *
     * <p>
     * Este método configura la seguridad HTTP para la aplicación. Se desactiva la protección CSRF y se configura
     * el comportamiento del inicio de sesión tanto para formularios como para autenticación OAuth2. Además, se añaden
     * filtros personalizados para validar el token JWT y manejar la autenticación de usuarios mediante OAuth2.
     * El inicio de sesión redirige a la URL "/holaseg" después de una autenticación exitosa.
     * </p>
     *
     * @param httpSecurity El objeto que se usa para configurar la seguridad HTTP en la aplicación.
     * @return Un {@link SecurityFilterChain} que define la configuración de seguridad para la aplicación.
     * @throws Exception Si ocurre algún error al configurar la seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                //Rutas que se excluyen del filtro de Spring(Pero ingresar a los filtros de JWT)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/auth/login",
                        "/api/auth/token/refresh",
                        "/api/auth/logout",
                        "/api/auth/password/reset-request",
                        "/api/auth/password/reset"
                        )
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                /*
                .formLogin(form -> form
                        .defaultSuccessUrl("/holaseg",true)) //Redirección luego de autenticación.

                 */

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //Se agregan filtros Personalizados.
                .addFilterBefore(new JwtTokenValidator(jwtUtils, messageService, systemLogService),UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new OAuth2UserFilter(jwtUtils,userRepository,messageService,refreshTokenService), BasicAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                      .defaultSuccessUrl("/holaseg",true))//Redirección luego de autenticación.

                .build();
    }


    /**
     * Crea el gestor de autenticación para la aplicación.
     *
     * <p>
     * Este método define un {@link AuthenticationManager} que es responsable de manejar los procesos de autenticación
     * en la aplicación. El gestor de autenticación es necesario para validar las credenciales de los usuarios durante
     * el proceso de inicio de sesión. Se obtiene utilizando la configuración de autenticación proporcionada por Spring.
     * </p>
     *
     * @param authenticationConfiguration La configuración de autenticación proporcionada por Spring.
     * @return Un {@link AuthenticationManager} que maneja la autenticación de los usuarios en la aplicación.
     * @throws Exception Si ocurre un error al crear el gestor de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * Crea un proveedor de autenticación para la aplicación.
     *
     * <p>
     * Este método configura un {@link AuthenticationProvider} que utiliza un {@link UserDetailsService} para obtener
     * los detalles del usuario durante el proceso de autenticación. El proveedor también establece un codificador de
     * contraseñas para garantizar que las contraseñas almacenadas en el sistema estén correctamente codificadas y validadas.
     * </p>
     *
     * @param userDetailsService El servicio que proporciona los detalles del usuario para la autenticación.
     * @return Un {@link AuthenticationProvider} configurado con el servicio de detalles del usuario y el codificador de contraseñas.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());

        provider.setUserDetailsService(userDetailsService);

        return provider;
    }



    /**
     * Crea un codificador de contraseñas para la aplicación.
     *
     * <p>
     * Este método configura un {@link PasswordEncoder} utilizando el algoritmo {@link BCryptPasswordEncoder}.
     * El codificador BCrypt proporciona un mecanismo robusto para almacenar contraseñas de manera segura,
     * aplicando un proceso de hashing con sal (salt) para mejorar la seguridad.
     * </p>
     *
     * @return Un {@link PasswordEncoder} configurado para codificar las contraseñas de forma segura.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
         return new BCryptPasswordEncoder();
    }


}