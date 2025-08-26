package com.odontologiaintegralfm.configuration.securityconfig.core;

import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.shared.enums.SystemUserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


/**
 * Genera un usuario autenticado para loguear en tareas programadas
 */
@Component
public class AuthenticatedSystemService {
    @Autowired
    private IUserService userService;

    public UserSec getAuthenticatedUserSystem() {

        // Traigo el usuario de sistema desde la base
        UserSec systemUser = userService.getByIdInternal(SystemUserId.SYSTEM.getId());

        // Creo un authentication válido con el usuario de sistema
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(systemUser, null, null);

        // Lo seteo en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Retorno el usuario para usarlo directamente
        return systemUser;
    }

}
