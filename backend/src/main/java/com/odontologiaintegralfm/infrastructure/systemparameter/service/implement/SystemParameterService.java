package com.odontologiaintegralfm.infrastructure.systemparameter.service.implement;


import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.infrastructure.systemparameter.model.SystemParameter;
import com.odontologiaintegralfm.infrastructure.systemparameter.repository.ISystemParameterRepository;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces.ISystemParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
public class SystemParameterService implements ISystemParameterService {

    @Autowired
    private ISystemParameterRepository systemParameterRepository;

    /**
     * Obtiene todas las parametrizaciones del sistema.
     *
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<SystemParameter> getAll() {
        try{
            return systemParameterRepository.findAll();
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "SystemParameterService",null, null, "getAll");
        }
    }

    /**
     * Obtiene un objeto parámetro de sistema por su ID.
     *
     * @param id del parámetro.
     * @return SystemParameter
     */
    @Override
    @Transactional(readOnly = true)
    public SystemParameter getById(Long id) {
        try{
            return systemParameterRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("exception.systemParameterService.notFound.user", null, "exception.systemParameterService.notFound.log", new Object[]{id, "SystemParameterService", "getById"}, LogLevel.ERROR));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "SystemParameterService",id, null, "getById");
        }
    }

    /**
     * Obtiene valor para por key de parámetro.
     * Se utiliza en métodos internos. Ej: Obtener en el JWTUtil el tiempo de expiración del JWT.
     *
     * @param keyName
     * @return
     */
    @Override
    public String getByKey(SystemParameterKey keyName) {
        try{

            return systemParameterRepository.findValueByKeyName(keyName);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "SystemParameterService",null, keyName.toString(), "getByKey");
        }
    }

    /**
     * Actualiza valor de un parámetro del sistema.
     * @param systemParameter DTO del parámetro a actualizar.
     * @return SystemParameter
     */
    @Override
    public SystemParameter update(SystemParameter systemParameter) {
        try{
            return systemParameterRepository.save(systemParameter);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "SystemParameterService",systemParameter.getId(), null, "update");
        }
    }
}
