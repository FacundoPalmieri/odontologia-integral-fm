package com.odontologiaintegralfm.scheduler;

import com.odontologiaintegralfm.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que contiene la lógica de limpieza de datos huérfanos:
 * - Domicilios.
 * - Contacto Telefónico.
 * - Contacto Email.
 * - Archivos adjuntos.
 * Esta tarea ya no tiene una expresión cron fija.
 * Su ejecución es programada dinámicamente desde SchedulerInitializerConfig.
 */
@Service
public class CleanupService {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IContactEmailService contactEmailService;

    @Autowired
    private IContactPhoneService contactPhoneService;

    @Autowired
    private IAttachedFilesService attachedFileService;


    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los domicilios huérfanos de la base.
     */
    @Transactional
    public void cleanOrphanDataAddress() {
            addressService.deleteOrphan();
    }

    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los emails huérfanos de la base.
     */
    @Transactional
    public void cleanOrphanDataContactEmail() {
        contactEmailService.deleteOrphan();
    }

    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los teléfonos huérfanos de la base.
     */
    @Transactional
    public void cleanOrphanDataContactPhone() {
        contactPhoneService.deleteOrphan();
    }

    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los archivos adjuntos con baja lógica que posean una antigüedad mayor a la parametrizada en {@link com.odontologiaintegralfm.model.AttachedFileConfig}.
     */
    public void cleanAttachedFileConfig() {
        attachedFileService.deleteAttachedFiles();
    }

}
