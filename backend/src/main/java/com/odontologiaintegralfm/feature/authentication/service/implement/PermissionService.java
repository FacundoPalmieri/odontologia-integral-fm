package com.odontologiaintegralfm.feature.authentication.service.implement;

import com.odontologiaintegralfm.feature.authentication.dto.PermissionFullResponseDTO;
import com.odontologiaintegralfm.feature.authentication.dto.PermissionSimpleResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.authentication.model.Permission;
import com.odontologiaintegralfm.feature.authentication.repository.IPermissionRepository;
import com.odontologiaintegralfm.infrastructure.message.service.implement.MessageService;
import com.odontologiaintegralfm.infrastructure.message.service.interfaces.IMessageService;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.*;

/**
 * Servicio que gestiona las operaciones relacionadas con los permisos del sistema.
 * <p>
 * El servicio proporciona métodos para recuperar todos los permisos, obtener un permiso por su ID,
 * y realizar consultas internas de permisos. Los permisos son representados mediante la entidad {@link Permission},
 * y los datos son transferidos mediante objetos DTO como {@link PermissionFullResponseDTO}.
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
     * @return Un objeto {@link Response} que contiene la lista de permisos en formato {@link PermissionFullResponseDTO}.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */

    @Override
    public Response<List<PermissionSimpleResponseDTO>> getAll() {
        try{

            List<Permission> permissionList = permissionRepository.findAll();

            List<PermissionSimpleResponseDTO> permissionSimpleResponseDTOList = new ArrayList<>();
            for(Permission permission : permissionList) {
                permissionSimpleResponseDTOList.add(convertToSimpleDTO(permission));
            }

            String messageUser = messageService.getMessage("permissionService.getAll.ok", null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser, permissionSimpleResponseDTOList);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e,"PermissionService", 0L,"","getAll");

        }
    }



    /**
     * Obtiene un permiso por su identificador único y devuelve la información al endpoint.
     * <p>
     * Este método busca un permiso en la base de datos mediante su ID. Si el permiso existe,
     * se convierte a un objeto {@link PermissionFullResponseDTO} y se devuelve dentro de un
     * objeto {@link Response}, junto con un mensaje de éxito obtenido del servicio de mensajes.

     * </p>
     * <p>
     * Si el permiso no se encuentra, lanza una excepción {@link NotFoundException}.
     * En caso de error de acceso a la base de datos o de transacción, lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param id El identificador único del permiso a buscar.
     * @return Un objeto {@link Response} que contiene el permiso en formato {@link PermissionFullResponseDTO}.
     * @throws NotFoundException Si no se encuentra un permiso con el ID especificado.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */




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
             new NotFoundException("exception.refreshTokenConfigNotFoundException.user",null,"exception.refreshTokenConfigNotFoundException.log",new Object[]{id,"Refresh Token Config Service", "getExpiration"}, LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"PermissionService", id, "","getByIdInternal");
        }
    }



    /**
     * Convierte una entidad {@link Permission} a un objeto de transferencia de datos (DTO).
     * <p>
     * Este método crea una instancia de {@link PermissionFullResponseDTO} y transfiere los datos
     * desde la entidad {@link Permission}.
     * </p>
     *
     * @param permission La entidad {@link Permission} a convertir.
     * @return Un objeto {@link PermissionFullResponseDTO} con los datos de la entidad.
     */

    private PermissionSimpleResponseDTO convertToSimpleDTO(Permission permission) {
        return new PermissionSimpleResponseDTO(
                permission.getId(),
                permission.getName(),
                permission.getLabel()
        );
    }




}
