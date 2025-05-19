package com.odontologiaintegralfm.configuration.securityConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.UnauthorizedException;
import com.odontologiaintegralfm.service.interfaces.ITokenConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Componente encargado de generar, validar y extraer información de tokens JWT (JSON Web Tokens).
 * No tiene lógica de negocio ni interactúa con la base de datos.
 * <p>
 * Esta clase proporciona métodos para:
 * <ul>
 *     <li>Crear un token JWT para un usuario autenticado</li>
 *     <li>Validar un token JWT y decodificarlo para obtener información contenida en él como el nombre de usuario (subject) y los claims.</li>
 *     <li>Extraer claims específicos</li>
 *     <li>Extraer todos los claims de un token decodificado.</li>
 * </ul>
 * </p>
 */
@Component
public class JwtUtils {

    private final MessageSource messageSource;
    //Con estas configuraciones aseguramos la autenticidad del token a crear
    @Value("${security.jwt.private.key}")
    private String privateKey;

    @Value("${security.jwt.user.generator}")
    private String userGenerator;

    @Autowired
    private ITokenConfigService tokenService;

    public JwtUtils(@Qualifier("messageSource") MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    /**
     * Crea un token JWT (JSON Web Token) para un usuario autenticado.
     * <p>
     * Este método genera un token JWT utilizando el algoritmo HMAC256 con una clave secreta,
     * e incluye información sobre el usuario autenticado, como su nombre de usuario, roles/autorizaciones,
     * la fecha de emisión y la fecha de expiración del token.
     * </p>
     *
     * @param authentication La autenticación del usuario, que contiene el principal (nombre de usuario)
     *                      y las autoridades (roles/permisos).
     * @return El token JWT generado.
     */
    public String createToken (Authentication authentication) {

        // Clave secreta y algoritmo para la firma del token
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

        // Obtener el nombre de usuario desde la autenticación
        String username = authentication.getPrincipal().toString();

        // Obtener las autoridades (permisos/roles) del usuario y convertirlas a una cadena separada por comas
        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Obtener y convertir el tiempo de expiración a un objeto Date antes de crear el Token.
        Date expirationDate = new Date(System.currentTimeMillis() + tokenService.getExpiration());

        //Se genera el token
        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator) //acá va el usuario que genera el token
                .withSubject(username) // a quien se le genera el token
                .withClaim("authorities", authorities) //claims son los datos contraidos en el JWT
                .withIssuedAt(new Date()) //fecha de generación del token
                .withExpiresAt(expirationDate) //fecha de expiración, tiempo en milisegundos
                //.withExpiresAt(new Date(System.currentTimeMillis() + 1800000)) //fecha de expiración, tiempo en milisegundos
                .withJWTId(UUID.randomUUID().toString()) //id al token - que genere una random
                .withNotBefore(new Date (System.currentTimeMillis())) //desde cuando es válido (desde ahora en este caso)
                .sign(algorithm); //nuestra firma es la que creamos con la clave secreta

        return jwtToken;
    }



    /**
     * Valída y decodifica un token JWT (JSON Web Token).
     * <p>
     * Este método recibe un token JWT, lo valida utilizando el algoritmo HMAC256 y la clave secreta,
     * y verifica que el emisor del token coincida con el generador esperado. Si el token es válido,
     * lo decodifica y devuelve un objeto {@link DecodedJWT} con los datos contenidos en el token.
     * </p>
     *
     * @param token El token JWT a validar y decodificar.
     * @return El token decodificado como un objeto {@link DecodedJWT}, que contiene los detalles del token.
     */
    public DecodedJWT validateToken(String token) {
        try {
            // Algoritmo y clave secreta para la validación
            Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

            // Configurar el verificador del token con el emisor esperado
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(this.userGenerator)
                    .build(); //usa patrón builder

            // Si el token es válido, se decodifica y se devuelve el objeto DecodedJWT
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        }catch (Exception e){
            throw new UnauthorizedException("exception.jwtUtils.validateToken.error.user", null, "exception.jwtUtils.validateToken.error.log", new Object[]{"JwtUtils","UserService"}, LogLevel.ERROR);
        }
    }


    /**
     * Extrae el nombre de usuario (subject) de un token JWT decodificado.
     * <p>
     * Este método obtiene el nombre de usuario (subject) que fue establecido al generar el token JWT.
     * El subject es una de las reclamaciones estándar en un token JWT, y en este caso se utiliza para
     * almacenar el nombre de usuario del sujeto para el cual se generó el token.
     * </p>
     *
     * @param decodedJWT El objeto {@link DecodedJWT} que contiene el token JWT decodificado.
     * @return El nombre de usuario extraído del token JWT.
     * @throws NullPointerException Si el {@link DecodedJWT} no contiene un subject o si el subject es null.
     */
    public String extractUsername (DecodedJWT decodedJWT) {
        //el subject es el usuario según establecimos al crear el token
        return decodedJWT.getSubject().toString();
    }


    /**
     * Obtiene un claim específico de un token JWT decodificado.
     * <p>
     * Este método extrae el valor de un claim específico del token JWT decodificado.
     * Los claims son pares clave-valor que contienen información adicional sobre el token,
     * y este método permite obtener el valor asociado a un claim dado por su nombre.
     * </p>
     *
     * @param decodedJWT El objeto {@link DecodedJWT} que contiene el token JWT decodificado.
     * @param claimName El nombre del claim que se desea obtener.
     * @return El valor del claim solicitado como un objeto {@link Claim}.
     * @throws NullPointerException Si el token JWT no contiene el claim especificado o si el claimName es null.
     */
    public Claim getSpecificClaim (DecodedJWT decodedJWT, String claimName) {
        return decodedJWT.getClaim(claimName);
    }


    /**
     * Devuelve todos los claims de un token JWT decodificado.
     * <p>
     * Este método extrae todos los claims del token JWT decodificado. Los claims son pares clave-valor
     * que contienen información adicional sobre el token, y este método devuelve todos los claims presentes
     * en el token como un mapa de clave-valor.
     * </p>
     *
     * @param decodedJWT El objeto {@link DecodedJWT} que contiene el token JWT decodificado.
     * @return Un {@link Map} que contiene todos los claims del token, donde la clave es el nombre del claim
     *         y el valor es el {@link Claim} asociado.
     * @throws NullPointerException Si el token JWT es nulo.
     */
    public Map<String, Claim> returnAllClaims (DecodedJWT decodedJWT){
        return decodedJWT.getClaims();
    }
}
