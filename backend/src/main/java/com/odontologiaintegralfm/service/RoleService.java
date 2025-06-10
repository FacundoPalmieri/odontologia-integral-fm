package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.RoleRequestDTO;
import com.odontologiaintegralfm.dto.RoleResponseDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.*;
import com.odontologiaintegralfm.model.Permission;
import com.odontologiaintegralfm.model.Role;
import com.odontologiaintegralfm.repository.IRoleRepository;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import com.odontologiaintegralfm.service.interfaces.IPermissionService;
import com.odontologiaintegralfm.service.interfaces.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


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


    /**
     * Recupera todos los roles almacenados en la base de datos.
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
    public Response<List<Role>> getAll() {
       try{
           List<Role> roleList =  roleRepository.findAll();

           String messageUser = messageService.getMessage("roleService.getAll.user.ok", null, LocaleContextHolder.getLocale());
           return new Response<>(true, messageUser, roleList);

       }catch(DataAccessException | CannotCreateTransactionException e){
           throw new DataBaseException(e,"roleService", 0L, "", "getAll");

       }
    }



    /**
     * Obtiene un rol por su identificador único y devuelve la información al endpoint.
     * <p>
     * Este método busca un rol en la base de datos mediante su ID. Si el rol existe,
     * se convierte a un objeto {@link RoleResponseDTO} y se devuelve dentro de un
     * objeto {@link Response}, junto con un mensaje de éxito obtenido del servicio de mensajes.
     * </p>
     * <p>
     * Si el rol no se encuentra, lanza una excepción {@link NotFoundException}.
     * En caso de error de acceso a la base de datos o de transacción, lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param id El identificador único del rol a buscar.
     * @return Un objeto {@link Response} que contiene el rol en formato {@link RoleResponseDTO}.
     * @throws NotFoundException Si no se encuentra un rol con el ID especificado.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */

    @Override
    public Response<RoleResponseDTO> getById(Long id) {
        try{
              Optional<Role> role = roleRepository.findById(id);
              if(role.isPresent()){
                  RoleResponseDTO dto = convertToDTOResponse(role.get());

                  String messageUser = messageService.getMessage("roleService.getById.user.ok", null, LocaleContextHolder.getLocale());
                  return new Response<>(true, messageUser, dto);
              }else{
                  throw new NotFoundException("exception.roleNotFound.user",null,"exception.roleNotFound.log", new Object[]{id,"RoleService", "getById" }, LogLevel.ERROR);
              }

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"roleService", id, "", "getById");

        }
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
     * y convierte la entidad guardada en un objeto {@link RoleResponseDTO},
     * el cual es devuelto junto con un mensaje de éxito.
     * </p>
     * <p>
     * Si ocurre un error durante el proceso de guardado o de acceso a la base de datos,
     * se lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param roleRequestDto El objeto DTO que contiene los datos del rol a guardar.
     * @return Un objeto {@link Response} que contiene el rol guardado en formato {@link RoleResponseDTO}.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */
    @Override
    public Response<RoleResponseDTO> create(RoleRequestDTO roleRequestDto) {

        //Valída que el rol no exista en la base de datos.
        validateRoleNotExist(roleRequestDto.getRole());

        //Asigna permisos al rol
        roleRequestDto.setPermissionsList(getPermissionForRole(roleRequestDto.getPermissionsList()));

        //Se construye el Objeto model
        Role role = buildRole(roleRequestDto);

        try{
            //Guarda el objeto en la base de datos.
            Role savedRole = roleRepository.save(role);

            //Conviente la entidad a un DTO
            RoleResponseDTO savedRoleDto = convertToDTOResponse(savedRole);

            String messageUser = messageService.getMessage("roleService.save.ok", null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser, savedRoleDto);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"roleService", 0L, roleRequestDto.getRole(), "save");

        }
    }


    /**
     * Actualiza la lista de permisos para el rol.
     *
     * @param roleRequestDto {@link RoleRequestDTO} que contiene la lista de permisos.
     * @return Un objeto {@link Response} que contiene el rol actualizado como un{@link RoleResponseDTO}
     */
    @Override
    public Response<RoleResponseDTO> update(RoleRequestDTO roleRequestDto) {

        //Valída que el rol exista en la base de datos.
        Role role = validateRoleExist(roleRequestDto.getRole());

        //Se limpia la lista de permisos
        role.getPermissionsList().clear();

        //Actualiza en el objeto role la lista obtenida de base de datos con la lista que poseé el DTO.
        role.setPermissionsList(roleRequestDto.getPermissionsList());

        //Valída que existan todos los permisos del DTO en la base de datos.
        roleRequestDto.getPermissionsList()
                .forEach(permissionId -> {
                    permissionService.getByIdInternal(permissionId.getId());
                });

        try{
            //Guarda el objeto en la base de datos.
            Role savedRole = roleRepository.save(role);

            //Conviente la entidad a un DTO
            RoleResponseDTO savedRoleDto = convertToDTOResponse(savedRole);

            String messageUser = messageService.getMessage("roleService.update.ok", null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser, savedRoleDto);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"roleService", 0L, roleRequestDto.getRole(), "update");

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
        Optional<Role> role = roleRepository.findRoleEntityByRole(roleNew);
        if(role.isPresent()) {
            if (role.get().getRole().equals(roleNew)) {
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
        return roleRepository.findRoleEntityByRole(roleUpdate).orElseThrow(()-> new NotFoundException("exception.roleNotFound.user",null,"exception.roleNotFound.log",new Object[]{roleUpdate,"RoleService", "validateRoleExist"},LogLevel.ERROR ));
    }




    /**
     * Convierte una entidad {@link Role} a un objeto de transferencia de datos (DTO) {@link RoleResponseDTO}.
     * <p>
     * Este método transfiere los datos desde la entidad {@link Role} hacia un objeto {@link RoleResponseDTO}.
     * Los datos transferidos incluyen el ID del rol, el nombre del rol y la lista de permisos asociados a dicho rol.
     * </p>
     *
     * @param role La entidad {@link Role} a convertir.
     * @return Un objeto {@link RoleResponseDTO} que contiene los datos de la entidad {@link Role}.
     */
    private RoleResponseDTO convertToDTOResponse(Role role) {
        RoleResponseDTO roleDTO = new RoleResponseDTO();
        roleDTO.setId(role.getId());
        roleDTO.setPermissionsList(role.getPermissionsList());
        return roleDTO;
    }




    /**
     * Recupera y valida un conjunto de permisos para un rol.
     * <p>
     * Este método toma un conjunto de permisos, busca cada uno en el sistema usando su ID, y los agrega a un conjunto de permisos válidos.
     * Si un permiso no se encuentra, se lanza una excepción {@link BadRequestException}.
     * </p>
     *
     * @param permissions Un conjunto de permisos que se desea asignar al rol.
     * @return Un conjunto {@link Set} de permisos válidos que se han encontrado en el sistema.
     * @throws BadRequestException Si no se encuentra un permiso con el ID proporcionado.
     */
    private Set<Permission> getPermissionForRole(Set<Permission> permissions) {
        Set<Permission> validPermission = new HashSet<>();
        for (Permission permission : permissions) {
            Permission foundPermission = permissionService.getByIdInternal(permission.getId());
            validPermission.add(foundPermission);
        }
        return validPermission;
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
                .role(roleRequestDto.getRole())
                .permissionsList(roleRequestDto.getPermissionsList())
                .build();
    }



    /*

    @Override
    public void deleteById(Long id) {
        try{
            roleRepository.deleteById(id);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"roleService", id, "", "delete");

        }
    }

    */
}
