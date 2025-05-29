package com.odontologiaintegralfm.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tarea programada que se encarga de limpieza de datos huérfanos.
 * Se ejecuta el primer día de cada mes a las 21:00 Hs.
 * - Domicilios.
 * - Contacto Telefónico.
 * - Contacto Email.
 */
@Service
public class CleanupService {

    @Scheduled(cron = "0 0 21 1 * *")
    @Transactional
    public void cleanOrphanData() {

    }

}
