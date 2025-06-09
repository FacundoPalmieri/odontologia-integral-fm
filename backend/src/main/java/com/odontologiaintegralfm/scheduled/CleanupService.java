package com.odontologiaintegralfm.scheduled;

import com.odontologiaintegralfm.service.interfaces.IAddressService;
import com.odontologiaintegralfm.service.interfaces.IContactEmailService;
import com.odontologiaintegralfm.service.interfaces.IContactPhoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que contiene la lógica de limpieza de datos huérfanos:
 * - Domicilios.
 * - Contacto Telefónico.
 * - Contacto Email.
 *
 * Esta tarea ya no tiene una expresión cron fija.
 * Su ejecución es programada dinámicamente desde DynamicScheduledTaskConfig.
 */
@Service
public class CleanupService {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IContactEmailService contactEmailService;

    @Autowired
    private IContactPhoneService contactPhoneService;


    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los datos huérfanos de la base.
     */
    @Transactional
    public void cleanOrphanData() {
            addressService.deleteOrphan();
            contactEmailService.deleteOrphan();
            contactPhoneService.deleteOrphan();
    }

}
