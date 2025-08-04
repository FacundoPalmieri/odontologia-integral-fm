package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.configuration.appConfig.annotations.LogAction;
import com.odontologiaintegralfm.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.LogType;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.repository.IContactEmailRepository;
import com.odontologiaintegralfm.service.interfaces.IContactEmailService;
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
public class ContactEmailService implements IContactEmailService {
    @Autowired
    private IContactEmailRepository contactEmailRepository;


    /**
     * Método para buscar o crear un contacto email y persistirlo en la base de datos.
     * @param emails a agregar
     * @return {@link ContactEmail}
     */
    @Transactional
    public Set<ContactEmail> findOrCreate(Set<String> emails) {
        Set<ContactEmail> contactEmails = new HashSet<>();
        try {
            for (String email : emails) {
                Optional<ContactEmail> existing = contactEmailRepository.findByEmail(email);
                if (existing.isPresent()) {
                    contactEmails.add(existing.get());
                } else {
                    ContactEmail newContactEmail = new ContactEmail();
                    newContactEmail.setEmail(email);
                    ContactEmail saved = contactEmailRepository.save(newContactEmail);
                    contactEmails.add(saved);
                }
            }
            return contactEmails;
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactEmailService", null, String.join(", ", emails), "create");
        }
    }

    /**
     * Realiza una eliminación física de los emails huérfanos.
     * Este método es invocado desde tareas programadas.
     *
     * @return
     */
    @Override
    @Transactional
    @LogAction(
            value ="contactEmailService.systemLogService.deleteOrphan",
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
            List<ContactEmail> orphanEmails = contactEmailRepository.findOrphan();

            //1. SI NO HAY REGISTROS PARA ELIMINAR
            if(orphanEmails.isEmpty()){
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
            int count = orphanEmails.size();

            //Se eliminan registros.
            contactEmailRepository.deleteAll(orphanEmails);

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
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactEmailService", null,null, "deleteOrphan");
        }
    }

}
