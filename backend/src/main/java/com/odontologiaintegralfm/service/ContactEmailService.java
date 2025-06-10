package com.odontologiaintegralfm.service;


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
    public void deleteOrphan() {
        try{
            List<ContactEmail> orphanEmails = contactEmailRepository.findOrphan();

            if(orphanEmails.isEmpty()){
                log.info("Tarea Programada: [Emails huérfanos] - No se encontraron registros para eliminar.");
                return;
            }
            //Se obtiene total para loguear.
            int count = orphanEmails.size();

            //Se elimina
            contactEmailRepository.deleteAll(orphanEmails);

            log.info("Tarea Programada: [Emails huérfanos] - [Total eliminados:  {}]", count);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactEmailService", null,null, "deleteOrphan");
        }
    }

}
