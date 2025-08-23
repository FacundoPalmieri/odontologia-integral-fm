package com.odontologiaintegralfm.feature.authentication.service.implement;

import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.feature.authentication.dto.*;
import com.odontologiaintegralfm.feature.authentication.model.Role;
import com.odontologiaintegralfm.feature.authentication.model.RolePermissionAction;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IPermissionService;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IRolePermissionActionService;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IRoleService;
import com.odontologiaintegralfm.feature.authentication.repository.IRoleRepository;
import com.odontologiaintegralfm.infrastructure.message.service.implement.MessageService;
import com.odontologiaintegralfm.infrastructure.message.service.interfaces.IMessageService;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Servicio que gestiona la lógica de negocio relacionada con los roles dentro del sistema.
 * Este servicio proporciona métodos para recuperar, crear y validar roles, así como para manejar
 * la asignación de permisos a los mismos. Los roles se gestionan mediante un repositorio y las
 * respuestas se devuelven a través de objetos de transferencia de datos (DTOs).
 * <p>
 * Los métodos de este servicio manejan excepciones específicas de base de datos y de validación
 * de roles, y utilizan el servicio de mensajes para proporcionar retroalimentación al usuario.
 * </p>
 *
 * <p>
 * El servicio interactúa con:
 * <ul>
 *   <li>{@link IRoleRepository} para la gestión de roles en la base de datos.</li>
 *   <li>{@link IPermissionService} para validar y asignar permisos a los roles.</li>
 *   <li>{@link IMessageService} para gestionar mensajes de éxito y error.</li>
 * </ul>
 * </p>
 *
 * Los métodos principales incluyen:
 * <ul>
 *   <li>findAll(): Recupera todos los roles.</li>
 *   <li>getById(): Obtiene un rol por su ID.</li>
 *   <li>findById(): Busca un rol por su ID sin generar una respuesta de tipo "response".</li>
 *   <li>save(): Guarda un nuevo rol después de validar su existencia y asignar permisos.</li>
 * </ul>
 */
@Service
public class RoleService implements IRoleService {

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IPermissionService permissionService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IRolePermissionActionService rolePermissionActionService;

    @Autowired
    private ActionService actionService;


    /**
     * Recupera todos los roles almacenados en la base de datos excpeto el rol DESARROLLADOR.
     * <p>
     * Este método obtiene la lista de roles desde el repositorio y la devuelve dentro de un objeto {@link Response}.
     * Además, genera un mensaje de éxito utilizando el servicio de mensajes {@link MessageService}.
     * </p>
     * <p>
     * En caso de error de acceso a la base de datos o de transacción, se lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @return Un objeto {@link Response} que contiene la lista de roles en formato {@link List<Role>}.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */

    @Override
    public Response<Set<RoleSimpleResponseDTO>> getAll() {
       try{
           Set<Role> roleSet =  roleRepository.findAllExcludingDevelopers(1L);

           Set<RoleSimpleResponseDTO> roleSimpleResponseDTO = convertToSimpleDTO(roleSet);


           String messageUser = messageService.getMessage("roleService.getAll.user.ok", null, LocaleContextHolder.getLocale());
           return new Response<>(true, messageUser, roleSimpleResponseDTO);

       }catch(DataAccessException | CannotCreateTransactionException e){
           throw new DataBaseException(e,"roleService", 0L, "", "getAll");

       }
    }


    /**
     * Método para obtener los permisos y acciones asociados a un Rol específico.
     * A partir de la lista de roles del usuario, este método obtiene todas las combinaciones
     * {@code RolePermissionAction} asociadas a cada rol, y agrupa las acciones por permiso
     * y los permisos por rol. El resultado es una lista de {@code RoleFullResponseDTO}, donde cada
     * rol contiene sus permisos y cada permiso contiene sus acciones correspondientes.
     *
     * <p>Ejemplo de estructura devuelta:</p>
     * <pre>
     * [
     *   {
     *     "id": 1,
     *     "name": "ADMIN",
     *     "permissionsList": [
     *       {
     *         "id": 10,
     *         "permission": "USERS",
     *         "name": "Gestión de usuarios",
     *         "actions": [
     *           { "id": 100, "action": "READ" },
     *           { "id": 101, "action": "WRITE" }
     *         ]
     *       }
     *     ]
     *   }
     * ]
     * </pre>
     *
     * No se arma el objeto Response aquí, porque este método es consumido también por el método loadUserByUsername de UserDetailServiceImpl.
     *
     * @param idRole
     * @return
     */
    @Override
    public RoleFullResponseDTO getFullByRoleId(Long idRole) {

        //Obtiene la entidad
        Role role = this.getByIdInternal(idRole);

        //Obtiene la relación persistida desde la base de datos.
        Set<RolePermissionAction> savedRolePermissionAction = rolePermissionActionService.getAllByRoleId(role.getId());

        // Mapa para agrupar acciones por permiso
        Map<Long, PermissionFullResponseDTO> permissionMap = new TreeMap<>();

        for(RolePermissionAction rpa : savedRolePermissionAction){
            Long permissionId = rpa.getPermission().getId();

            // Si no existe el permiso en el mapa, lo agrego
            permissionMap.putIfAbsent(permissionId, new PermissionFullResponseDTO(
                    permissionId,
                    rpa.getPermission().getName(),
                    rpa.getPermission().getLabel(),
                    new TreeSet<>()
            ));

            // Agrego acción al permiso correspondiente
            PermissionFullResponseDTO permDTO = permissionMap.get(permissionId);
            permDTO.getActions().add(new ActionResponseDTO(
                    rpa.getAction().getId(),
                    rpa.getAction().getName(),
                    rpa.getAction().getLabel()
            ));

        }

        //Convierte el MAP en SET
        Set<PermissionFullResponseDTO> Permissions = new LinkedHashSet<>(permissionMap.values());

        // Arma el name con su lista de permisos
        RoleFullResponseDTO roleFullResponseDTO = new RoleFullResponseDTO();
        roleFullResponseDTO.setId(role.getId());
        roleFullResponseDTO.setName(role.getName());
        roleFullResponseDTO.setLabel(role.getLabel());
        roleFullResponseDTO.setPermissionsList(Permissions);

        return roleFullResponseDTO;
    }



    /**
     * Busca un rol por su identificador único en la base de datos. Este método solo es consumido por otros servicios que no necesitan una respuesta de tipo "response".
     * <p>
     * Este método consulta al repositorio para encontrar un rol con el ID proporcionado.
     * Si se encuentra, devuelve un {@link Optional} con la entidad {@link Role},
     * en caso contrario, devuelve un {@link Optional} vacío.
     * </p>
     * <p>
     * En caso de error de acceso a la base de datos o de transacción, lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param id El identificador único del rol a buscar.
     * @return Un {@link Optional} que contiene el rol si se encuentra, o vacío si no existe.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */
    @Override
    public Role getByIdInternal(Long id) {
        try{
            return roleRepository.findById(id).orElseThrow(()->
                    new BadRequestException("exception.roleNotFoundUserCreationException.user",null,"exception.roleNotFoundUserCreationException.log",new Object[]{id,"RoleService", "getByIdInternal"},LogLevel.ERROR));

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"roleService", id, "", "getByIdInternal");

        }
    }


    /**
     * Guarda un nuevo rol en la base de datos.
     * <p>
     * Este método valida si el rol ya existe en la base de datos, asigna permisos al rol,
     * y luego construye el objeto {@link Role}. Después, guarda el rol en la base de datos
     * y convierte la entidad guardada en un objeto {@link RoleFullResponseDTO},
     * el cual es devuelto junto con un mensaje de éxito.
     * </p>
     * <p>
     * Si ocurre un error durante el proceso de guardado o de acceso a la base de datos,
     * se lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param roleRequestDto El objeto DTO que contiene los datos del rol a guardar.
     * @return Un objeto {@link Response} que contiene el rol guardado en formato {@link RoleFullResponseDTO}.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */
    @Override
    @Transactional
    @LogAction(
            value = "roleService.systemLogService.create",
            args = {"#roleRequestDto.name"},
            level = LogLevel.INFO,
            type = LogType.SYSTEM
    )
    public Response<RoleFullResponseDTO> create(RoleRequestDTO roleRequestDto) {
        try{
            //Valída que el rol no exista en la base de datos.
            validateRoleNotExist(roleRequestDto.getName());

            //Se construye el Objeto model
            Role role = buildRole(roleRequestDto);

            //Guarda el rol en la base de datos.
            Role savedRole = roleRepository.save(role);

            //Valída que existan todos los permisos del DTO en la base de datos.
            roleRequestDto.getPermissionsList()
                    .forEach(permissionId -> {
                        permissionService.getByIdInternal(permissionId.getPermissionId());
                    });

            //Crea las nuevas relaciones.
            rolePermissionActionService.buildRelationByRole(role, roleRequestDto.getPermissionsList());


            // Arma el árbol de respuesta entre rol, permisos y acciones.
            RoleFullResponseDTO roleFullResponseDTO = this.getFullByRoleId(savedRole.getId());

            String messageUser = messageService.getMessage("roleService.save.ok", null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser, roleFullResponseDTO);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"roleService", 0L, roleRequestDto.getName(), "save");

        }
    }


    /**
     * Actualiza la lista de permisos para el rol.
     *
     * @param roleRequestDto {@link RoleRequestDTO} que contiene la lista de permisos.
     * @return Un objeto {@link Response} que contiene el rol actualizado como un{@link RoleFullResponseDTO}
     */
    @Override
    @Transactional
    @LogAction(
            value = "roleService.systemLogService.update",
            args = {"#roleRequestDto.name"},
            level = LogLevel.INFO,
            type = LogType.SYSTEM
    )
    public Response<RoleFullResponseDTO> update(RoleRequestDTO roleRequestDto) {
        try{
            //Valída que el rol exista en la base de datos.
            Role role = validateRoleExist(roleRequestDto.getName());

            //Elimina las relaciones anteriores entre Rol, Permisos y acciones.
            rolePermissionActionService.deleteByRoleId(role.getId());

            //Valída que existan todos los permisos del DTO en la base de datos.
            roleRequestDto.getPermissionsList()
                    .forEach(permissionId -> {
                        permissionService.getByIdInternal(permissionId.getPermissionId());
                    });

            //Crea las nuevas relaciones.
            rolePermissionActionService.buildRelationByRole(role, roleRequestDto.getPermissionsList());

            // Arma el árbol de respuesta entre rol, permisos y acciones.
            RoleFullResponseDTO roleFullResponseDTO = this.getFullByRoleId(role.getId());

            String messageUser = messageService.getMessage("roleService.update.ok", null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser, roleFullResponseDTO);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"roleService", 0L, roleRequestDto.getName(), "update");

        }
    }



    /**
     * Valída que el rol especificado no exista ya en la base de datos.
     * <p>
     * Este método consulta el repositorio para verificar si el rol proporcionado ya existe.
     * Si el rol ya existe, lanza una excepción {@link ConflictException} para evitar duplicados.
     * </p>
     *
     * @param roleNew El nombre del rol a validar.
     * @throws ConflictException Si el rol ya existe en la base de datos.
     */
    private void validateRoleNotExist(String roleNew){
        Optional<Role> role = roleRepository.findRoleEntityByName(roleNew);
        if(role.isPresent()) {
            if (role.get().getName().equals(roleNew)) {
                throw new ConflictException("exception.roleExisting.user",new Object[]{roleNew},"exception.roleExisting.log",new Object[]{roleNew,"RoleService", "Save"},LogLevel.ERROR);
            }
        }
    }


    /**
     * Valída que el rol especificado exista en la base de datos.
     * <p>
     * Este método consulta el repositorio para verificar si el rol proporcionado ya existe para poder modificarlo.
     * Si el rol no fue encontrando,lanza una excepción {@link NotFoundException  }
     * </p>
     *
     * @param roleUpdate El nombre del rol a validar.
     * @throws NotFoundException   Si el rol no fue encontrando en la base de datos.
     */
    private Role validateRoleExist(String roleUpdate){
        return roleRepository.findRoleEntityByName(roleUpdate).orElseThrow(()-> new NotFoundException("exception.roleNotFound.user",null,"exception.roleNotFound.log",new Object[]{roleUpdate,"RoleService", "validateRoleExist"},LogLevel.ERROR ));
    }


    /**
     * Construye una entidad {@link Role} a partir de un objeto {@link RoleRequestDTO}.
     * <p>
     * Este método utiliza el patrón de diseño builder para crear una nueva instancia de {@link Role}.
     * Los valores del rol y la lista de permisos se toman del objeto {@link RoleRequestDTO}.
     * </p>
     *
     * @param roleRequestDto El objeto {@link RoleRequestDTO} que contiene los datos necesarios para crear la entidad {@link Role}.
     * @return Una nueva instancia de {@link Role} con los valores proporcionados en {@link RoleRequestDTO}.
     */
    private Role buildRole(RoleRequestDTO roleRequestDto) {
        return Role.builder()
                .name(roleRequestDto.getName())
                .label(roleRequestDto.getLabel())
                .build();
    }


    private Set <RoleSimpleResponseDTO> convertToSimpleDTO(Set <Role> roles) {

        Set<RoleSimpleResponseDTO> roleSimpleResponseDTOSet = roles.stream()
                .map(role -> new RoleSimpleResponseDTO(role.getId(), role.getName(), role.getLabel()))
                .collect(Collectors.toSet());

        return roleSimpleResponseDTOSet;
    }



}
