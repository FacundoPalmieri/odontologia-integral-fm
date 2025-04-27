package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.PermissionResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.Permission;
import com.odontologiaintegralfm.repository.IPermissionRepository;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import com.odontologiaintegralfm.service.interfaces.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que gestiona las operaciones relacionadas con los permisos del sistema.
 * <p>
 * El servicio proporciona métodos para recuperar todos los permisos, obtener un permiso por su ID,
 * y realizar consultas internas de permisos. Los permisos son representados mediante la entidad {@link Permission},
 * y los datos son transferidos mediante objetos DTO como {@link PermissionResponseDTO}.
 * </p>
 * <p>
 * Además, se utiliza el servicio de mensajes {@link MessageService} para generar mensajes informativos
 * de éxito o error, los cuales son retornados dentro de un objeto {@link Response}.
 * </p>
 * <p>
 * Este servicio maneja las excepciones de acceso a la base de datos o de transacción lanzando la excepción
 * {@link DataBaseException}, y en caso de no encontrar un permiso, lanza {@link NotFoundException}.
 * </p>
 */

@Service
public class PermissionService implements IPermissionService {

    @Autowired
    private IPermissionRepository permissionRepository;

    @Autowired
    private IMessageService messageService;

    /**
     * Recupera todos los permisos almacenados en la base de datos.
     * <p>
     * Este método obtiene la lista de permisos desde el repositorio, los convierte a DTO y
     * los devuelve dentro de un objeto {@link Response}. Además, genera un mensaje de éxito
     * utilizando el servicio de mensajes {@link MessageService}.
     * </p>
     * <p>
     * En caso de error de acceso a la base de datos o de transacción, se lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @return Un objeto {@link Response} que contiene la lista de permisos en formato {@link PermissionResponseDTO}.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */

    @Override
    public Response<List<PermissionResponseDTO>> getAll() {
        try{

            List<Permission> permissionList = permissionRepository.findAll();

            List<PermissionResponseDTO> permissionResponseDTOList = new ArrayList<>();
            for(Permission permission : permissionList) {
                permissionResponseDTOList.add(convertToDTO(permission));
            }

            String messageUser = messageService.getMessage("permissionService.getAll.ok", null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser, permissionResponseDTOList);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e,"PermissionService", 0L,"","getAll");

        }
    }



    /**
     * Obtiene un permiso por su identificador único y devuelve la información al endpoint.
     * <p>
     * Este método busca un permiso en la base de datos mediante su ID. Si el permiso existe,
     * se convierte a un objeto {@link PermissionResponseDTO} y se devuelve dentro de un
     * objeto {@link Response}, junto con un mensaje de éxito obtenido del servicio de mensajes.

     * </p>
     * <p>
     * Si el permiso no se encuentra, lanza una excepción {@link NotFoundException}.
     * En caso de error de acceso a la base de datos o de transacción, lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param id El identificador único del permiso a buscar.
     * @return Un objeto {@link Response} que contiene el permiso en formato {@link PermissionResponseDTO}.
     * @throws NotFoundException Si no se encuentra un permiso con el ID especificado.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */
    @Override
    public Response<PermissionResponseDTO> getById(Long id) {
        try{
            Optional<Permission> permissionOptional = permissionRepository.findById(id);
            if(permissionOptional.isPresent()) {
                Permission permission = permissionOptional.get();
                PermissionResponseDTO permissionResponseDTO = convertToDTO(permission);

                String messageUser = messageService.getMessage("permissionService.getById.ok", null, LocaleContextHolder.getLocale());
                return new Response<>(true, messageUser, permissionResponseDTO);
            }else{
                throw new NotFoundException("","exception.permissionNotFound.user",null,"exception.permissionNotFound.log",id," ", "PermissionService","getById", LogLevel.ERROR);            }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e,"PermissionService", id,"","getById");

        }
    }




    /**
     * Busca un permiso por su identificador único en la base de datos. Se utiliza para consultas internas de otros métodos que no necesitan una respuesta de tipo "Response".
     * <p>
     * Este método consulta al repositorio para encontrar un permiso con el ID proporcionado.
     * Si se encuentra, devuelve un {@link Optional} con la entidad {@link Permission},
     * en caso contrario, devuelve un {@link Optional} vacío.
     * Se utiliza para consulta interna de otros métodos que no necesitan una respuesta de tipo "response"
     * </p>
     * <p>
     * En caso de error de acceso a la base de datos o de transacción, lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param id El identificador único del permiso a buscar.
     * @return Un {@link Optional} que contiene el permiso si se encuentra, o vacío si no existe.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */
    @Override
    public Permission getByIdInternal(Long id) {
        try{
            return permissionRepository.findById(id).orElseThrow(()->
             new NotFoundException("","exception.refreshTokenConfigNotFoundException.user",null,"exception.refreshTokenConfigNotFoundException.log", 0L, "","Refresh Token Config Service", "getExpiration", LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"PermissionService", id, "","getByIdInternal");

        }
    }





    /**
     * Convierte una entidad {@link Permission} a un objeto de transferencia de datos (DTO).
     * <p>
     * Este método crea una instancia de {@link PermissionResponseDTO} y transfiere los datos
     * desde la entidad {@link Permission}.
     * </p>
     *
     * @param permission La entidad {@link Permission} a convertir.
     * @return Un objeto {@link PermissionResponseDTO} con los datos de la entidad.
     */

    private PermissionResponseDTO convertToDTO(Permission permission) {
        PermissionResponseDTO permissionResponseDTO = new PermissionResponseDTO();
        permissionResponseDTO.setId(permission.getId());
        permissionResponseDTO.setPermission(permission.getPermission());
        permissionResponseDTO.setName(permission.getName());
        return permissionResponseDTO;
    }

/*
    @Override
    public Permission save(Permission permission) {
        try {
            return permissionRepository.save(permission);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"PermissionService", permission.getId(), permission.getPermissionName(), "save");

        }
    }

    @Override
    public void deleteById(Long id) {
        try{
            permissionRepository.deleteById(id);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"PermissionService", id, "", "delete");
        }
    }

    @Override
    public Permission update(Permission permission) {
        try{
            return permissionRepository.save(permission);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"PermissionService", permission.getId(), permission.getPermissionName(), "update");
        }
    }

 */
}
