package com.odontologiaintegralfm.feature.developer.service;


import com.odontologiaintegralfm.infrastructure.message.dto.MessageRequestDTO;
import com.odontologiaintegralfm.infrastructure.systemparameter.dto.SystemParameterRequestDTO;
import com.odontologiaintegralfm.infrastructure.systemparameter.dto.SystemParameterResponseDTO;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.infrastructure.message.model.MessageConfig;
import com.odontologiaintegralfm.infrastructure.logging.dto.SystemLogResponseDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.ScheduleRequestDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.ScheduleResponseDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.model.ScheduleTask;
import com.odontologiaintegralfm.infrastructure.systemparameter.model.SystemParameter;
import com.odontologiaintegralfm.infrastructure.logging.service.SystemLogService;
import com.odontologiaintegralfm.infrastructure.message.service.interfaces.IMessageService;
import com.odontologiaintegralfm.infrastructure.scheduler.service.IScheduleService;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces.ISystemParameterService;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Servicio encargado de gestionar la configuración de la aplicación relacionada con:
 * <ul>
 *     <li>Intentos fallidos de inicio de sesión. </li>
 *     <li>Mensajes configurables </li>
 *     <li>Expiración del token de autenticación. </li>
 * </ul>
 * <p>
 * Este servicio proporciona métodos para obtener y actualizar configuraciones como los intentos fallidos de inicio de sesión,
 * la expiración del token y los mensajes almacenados. Utiliza los servicios {@link IMessageService}, {@link },
 * , {@link }  y  {@link } para interactuar con las configuraciones subyacentes, construyendo respuestas que incluyen mensajes
 * para el usuario y los valores actualizados de las configuraciones.
 * </p>
 */
@Service
public class ConfigService implements IConfigService {

    @Autowired
    private IMessageService messageService;


    @Autowired
    private IScheduleService scheduleConfigService;


    @Autowired
    private ISystemParameterService systemParameterService;

    @Autowired
    private SystemLogService systemLogService;


    /**
     * Obtiene un listado de configuraciones de mensajes.
     * <p>Este método recupera una lista de configuraciones de mensajes desde el servicio {@link IMessageService},
     *y construye un mensaje para el usuario usando la clave "config.getMessage.ok".</p>
     * @return Una respuesta que contiene un mensaje de éxito y la lista de configuraciones de mensajes obtenida.
     */
    @Override
    public Response<List<MessageConfig>>getMessage(){

        //Obtiene el listado.
        List<MessageConfig> listMessage = messageService.listMessage();

        //Se construye Mensaje para usuario.
        String userMessage = messageService.getMessage("config.getMessage.ok", null, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage, listMessage);
    }



    /**
     * Actualiza un mensaje en la base de datos.
     *<p>
     * Este método valida la existencia de un mensaje a partir del ID proporcionado en el DTO {@link MessageRequestDTO},
     * luego actualiza la configuración de ese mensaje en la base de datos. Si la actualización es exitosa,
     * se construye un mensaje para el usuario y se devuelve junto con el mensaje actualizado.
     *</p>
     * @param messageRequestDto El DTO que contiene la información para actualizar el mensaje.
     * @return Una respuesta que contiene el mensaje actualizado y un mensaje de éxito para el usuario.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos o durante la transacción.
     */
    @Override
    @Transactional
    public Response<MessageConfig> updateMessage(MessageRequestDTO messageRequestDto) {
        try{
            // valída y Obtiene mensaje de BD
            MessageConfig message = messageService.getById(messageRequestDto.id());

            //Actualiza campo
            message.setValue(messageRequestDto.value());

            message  = messageService.updateMessage(message);

            //Prepara y envía respuesta.
            String userMessage = messageService.getMessage("config.updateMessage.ok",null,LocaleContextHolder.getLocale());

            return new Response<>(true, userMessage,message);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "configService", messageRequestDto.id(), "", "updateMessage");
        }

    }

    /**
     * Obtiene todas las parametrizaciones del sistema.
     *
     * @return SystemParameterResponseDTO
     */
    @Override
    @Transactional(readOnly = true)
    public Response<List<SystemParameterResponseDTO>> getSystemParameter() {
        try{
            List<SystemParameter> systemParameters = systemParameterService.getAll();
            List<SystemParameterResponseDTO> systemParameterResponseDTO = systemParameters
                    .stream()
                    .map(systemParameter -> new SystemParameterResponseDTO(
                            systemParameter.getId(),
                            systemParameter.getValue(),
                            systemParameter.getDescription()
                    ))
                    .toList();

            return new Response<>(true, null, systemParameterResponseDTO);


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ConfigService",null, null, "getSystemParameter");
        }
    }

    /**
     * Actualiza valor de un parámetro del sistema.
     *
     * @param systemParameterRequestDTO
     * @return
     */
    @Override
    public Response<SystemParameterResponseDTO> updateSystemParameter(SystemParameterRequestDTO systemParameterRequestDTO) {
        try{
            //Obtiene el objeto de la base de datos.
            SystemParameter systemParameter= systemParameterService.getById(systemParameterRequestDTO.id());

            //Actualiza el Objeto con los valores del DTO.
            systemParameter.setValue(systemParameterRequestDTO.value());
            systemParameterService.update(systemParameter);

            //Construye DTO respuesta
            SystemParameterResponseDTO systemParameterResponseDTO = new SystemParameterResponseDTO(
                    systemParameter.getId(),
                    systemParameter.getValue(),
                    systemParameter.getDescription()
                    );


            //Obtiene mensaje de respuesta
            String messageUser= messageService.getMessage("config.update.ok",null,LocaleContextHolder.getLocale());

            //Elabora la respuesta.
            return new Response<>(true, messageUser,systemParameterResponseDTO);


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ConfigService",null, null, "updateSystemParameter");
        }
    }


    /**
     * Obtiene la regla que define cuándo se ejecutará la tarea programada.
     * <p>
     * La expresión se representa en el siguiente formato cron de 6 campos:
     * <ul>
     *     <li><b>Segundo</b> (0-59)</li>
     *     <li><b>Minuto</b> (0-59)</li>
     *     <li><b>Hora</b> (0-23)</li>
     *     <li><b>Día del mes</b> (1-31)</li>
     *     <li><b>Mes</b> (1-12 o JAN-DEC)</li>
     *     <li><b>Día de la semana</b> (0-6 o SUN-SAT)</li>
     * </ul>
     * Por ejemplo: {@code "0 0 21 1 * *"} ejecuta la tarea el día 1 de cada mes a las 21:00 hs.
     *
     * @return {@link Response} que contiene la expresión cron actual configurada.
     */
    @Override
    public Response<List<ScheduleResponseDTO>> getAllSchedule() {
       return new Response<>(true, null, scheduleConfigService.getAll());
    }



    /**
     * Actualiza la regla que define cuándo se ejecutará la tarea programada.
     * El método realiza lógica de validación y llama al servicio correspondiente para actualizar.
     *
     * @param scheduleRequestDTO regla expresada en:
     *                       <ul>
     *                           <li><b>Segundo</b> (0-59)</li>
     *                           <li><b>Minuto</b> (0-59)</li>
     *                           <li><b>Hora</b> (0-23)</li>
     *                           <li><b>Día del mes</b> (1-31)</li>
     *                           <li><b>Mes</b> (1-12 o JAN-DEC)</li>
     *                           <li><b>Día de la semana</b> (0-6 o SUN-SAT)</li>
     *                       </ul>
     *                       Por ejemplo: {@code "0 0 21 1 * *"} ejecuta la tarea el día 1 de cada mes a las 21:00 hs.
     * @return {@link Response}
     */
    @Override
    @LogAction(
            value = "config.systemLogService.updateSchedule",
            args = {"#scheduleRequestDTO.id"},
            type = LogType.SCHEDULED,
            level = LogLevel.INFO
    )
    public Response<ScheduleResponseDTO> updateSchedule(ScheduleRequestDTO scheduleRequestDTO) {
        try{
            //Validar que la tarea exista
            ScheduleResponseDTO scheduleConfigDTO = scheduleConfigService.getById(scheduleRequestDTO.id());

            //Actualiza la entidad para guardar en bd.
            ScheduleTask scheduleTask = new ScheduleTask();
            scheduleTask.setId(scheduleRequestDTO.id());
            scheduleTask.setLabel(scheduleConfigDTO.label());
            scheduleTask.setCronExpression(scheduleRequestDTO.cronExpression());

            //Persiste
            scheduleTask = scheduleConfigService.update(scheduleTask);

            //Carga el objeto Response.
            ScheduleResponseDTO scheduleResponseDTO = new ScheduleResponseDTO(
                    scheduleTask.getId(),
                    scheduleTask.getLabel(),
                    scheduleTask.getCronExpression()
            );

            String userMessage = messageService.getMessage("config.updateSchedule.ok", new Object[]{scheduleRequestDTO.cronExpression()}, LocaleContextHolder.getLocale());
            return new Response<>(true, userMessage, scheduleResponseDTO);


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "configService", scheduleRequestDTO.id(), "", "updateSchedule");
        }
    }

    /**
     * Se comunica con el servicio de {@link SystemLogService} para obtener los logs.
     *
     * @param pageValue
     * @param sizeValue
     * @param sortByValue
     * @param directionValue
     * @return
     */
    @Override
    public Response<Page<SystemLogResponseDTO>> getLogs(int pageValue, int sizeValue, String sortByValue, String directionValue) {
        try{
            return new Response<>(true, null, systemLogService.getAll(pageValue,sizeValue,sortByValue,directionValue));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "configService", null, null, "getAllLogs");
        }
    }


}
