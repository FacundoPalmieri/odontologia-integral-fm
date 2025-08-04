package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.configuration.appConfig.annotations.LogAction;
import com.odontologiaintegralfm.dto.ContactPhoneRequestDTO;
import com.odontologiaintegralfm.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.LogType;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.ContactPhone;
import com.odontologiaintegralfm.repository.IContactPhoneRepository;
import com.odontologiaintegralfm.service.interfaces.IContactPhoneService;
import com.odontologiaintegralfm.service.interfaces.IPhoneTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ContactPhoneService implements IContactPhoneService {
    @Autowired
    private IContactPhoneRepository contactPhoneRepository;

    @Autowired
    private IPhoneTypeService phoneTypeService;


    /**
     * Método para buscar o  crear un contacto telefónico y persistirlo en la base de datos.
     *
     * @param phoneDTOs Tipo y número de teléfono
     * @return {@link ContactPhone}
     */
    @Override
    @Transactional
    public Set<ContactPhone> findOrCreate(Set<ContactPhoneRequestDTO> phoneDTOs) {
        Set<ContactPhone> contactPhones = new HashSet<>();

        for (ContactPhoneRequestDTO dto : phoneDTOs) {
            Optional<ContactPhone> existingPhone = contactPhoneRepository.findByNumber(dto.phone());
            if (existingPhone.isPresent()) {
                contactPhones.add(existingPhone.get());
            } else {
                ContactPhone newPhone = new ContactPhone();
                newPhone.setNumber(dto.phone());
                newPhone.setPhoneType(phoneTypeService.getById(dto.phoneType()));
                contactPhones.add(contactPhoneRepository.save(newPhone));
            }
        }
        return contactPhones;
    }

    /**
     * Realiza una eliminación física de los teléfonos huérfanos.
     * Este método es invocado desde tareas programadas.
     *
     * @return
     */
    @Override
    @Transactional
    @LogAction(
            value ="contactPhoneService.systemLogService.deleteOrphan",
            args =  {"#result.durationSeconds","#result.message", "#result.countInit","#result.countDeleted" },
            type = LogType.SCHEDULED,
            level = LogLevel.INFO
    )
    public SchedulerResultDTO deleteOrphan() {
        int countInit = 0;
        int countDeleted = 0;
        long start;
        long end;
        double durationSeconds;

        //Inicia tarea programada
        start = System.currentTimeMillis();

        try{
            List<ContactPhone> orphanPhone = contactPhoneRepository.findOrphan();

            //1. SI NO HAY REGISTROS PARA ELIMINAR
            if(orphanPhone.isEmpty()) {
                //Finaliza tarea programada
                end = System.currentTimeMillis();
                //Convierte milisegundos a segundos.
                durationSeconds = (end - start) / 1000.0;

                SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                        durationSeconds,
                        "No se encontraron registros para eliminar",
                        countInit,
                        countDeleted);
                return schedulerResultDTO;
            }

            //2. SI EXISTEN REGISTROS PARA ELIMINAR

            //Se obtiene total para loguear.
             countInit = orphanPhone.size();

            //Se eliminan registros.
            contactPhoneRepository.deleteAll(orphanPhone);

            //Finaliza tarea programada
            end = System.currentTimeMillis();

            //Convierte milisegundos a segundos.
            durationSeconds = (end - start) / 1000.0;

            SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                    durationSeconds,
                    "Registros eliminados correctamente",
                    countInit,
                    countDeleted);
            return schedulerResultDTO;

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactPhoneService", null,null, "deleteOrphan");
        }

    }


}
