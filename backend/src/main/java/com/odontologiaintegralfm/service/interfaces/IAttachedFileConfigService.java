package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.AttachedFileConfigRequestDTO;
import com.odontologiaintegralfm.model.AttachedFileConfig;

/**
 * @author [Facundo Palmieri]
 */
public interface IAttachedFileConfigService {

    /**
     * Método para obtener la configuración de días "mínimos" permitidos para un archivo adjunto antes de su baja física.
     * @return
     */
    AttachedFileConfig get();


    /**
     * Método para actualizar la configuración de días "mínimos" permitidos para un archivo adjunto antes de su baja física.
     * @return
     */
    AttachedFileConfig update(AttachedFileConfig attachedFileConfig);

}
