package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.AttachedFileConfigRequestDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.AttachedFileConfig;
import com.odontologiaintegralfm.repository.IAttachedFileConfigRepository;
import com.odontologiaintegralfm.service.interfaces.IAttachedFileConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class AttachedFileConfigService implements IAttachedFileConfigService {

    @Autowired
    private IAttachedFileConfigRepository attachedFileConfigRepository;

    /**
     * Método para obtener la configuración de días "minimos" permitidos para un archivo adjunto antes de su baja física.
     * @return
     */
    @Override
    public AttachedFileConfig get() {
        try{

            return attachedFileConfigRepository.findFirstBy()
                    .orElseThrow(() -> new NotFoundException("exception.attachedFileConfigNotFound.user",null, "exception.attachedFileConfigNotFound.log",new Object[]{"ConfigService","updateRefreshTokenExpiration"}, LogLevel.ERROR));


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileConfigService", null,null, "getAttachedFileConfig");
        }
    }

    /**
     * Método para actualizar la configuración de días "mínimos" permitidos para un archivo adjunto antes de su baja física.
     *
     * @param attachedFileConfig
     * @return
     */
    @Override
    public AttachedFileConfig update(AttachedFileConfig attachedFileConfig) {
        try{

            return attachedFileConfigRepository.save(attachedFileConfig);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileConfigService", null,null, "getAttachedFileConfig");
        }
    }
}
