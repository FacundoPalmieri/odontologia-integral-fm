package com.odontologiaintegralfm.infrastructure.scheduler.task;

import com.odontologiaintegralfm.feature.person.core.service.intefaces.IAddressService;
import com.odontologiaintegralfm.feature.person.core.service.intefaces.IContactEmailService;
import com.odontologiaintegralfm.feature.person.core.service.intefaces.IContactPhoneService;
import com.odontologiaintegralfm.feature.person.core.service.intefaces.IAttachedFilesService;
import com.odontologiaintegralfm.infrastructure.logging.service.ISystemLogService;
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

    @Autowired
    private ISystemLogService systemLogService;


    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los domicilios huérfanos de la base.
     */
    @Transactional
    public void cleanOrphanDataAddress() {addressService.deleteOrphan();}

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
     * Elimina todos los archivos adjuntos con baja lógica que posean una antigüedad mayor a la parametrizada.
     */
    public void cleanAttachedFileDisabled() {
        attachedFileService.deleteAttachedFiles();
    }


    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina logs que posean una antigüedad mayor a la parametrizada.
     */
    public void cleanLogs(){
        systemLogService.delete();
    }

}
