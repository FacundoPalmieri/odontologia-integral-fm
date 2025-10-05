package com.odontologiaintegralfm.configuration.securityconfig.core;

import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.shared.enums.SystemUserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Genera un usuario para loguear en tareas programadas
 */
@Component
public class AuthenticatedSystemService {
    @Autowired
    private IUserService userService;

    public UserSec getAuthenticatedUserSystem() {
        return userService.getByIdInternal(SystemUserId.SYSTEM.getId());
    }

}
