package com.odontologiaintegralfm.configuration.securityconfig.core;

import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.feature.user.repository.IUserRepository;
import com.odontologiaintegralfm.shared.enums.SystemUserId;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserService {

    @Autowired
    private IUserRepository userRepository;

    /**
     * Retorna el usuario autenticado si existe.
     * Si no hay usuario en SecurityContext, devuelve el usuario SYSTEM.
     */
    public UserSec getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            Object principal = authentication.getPrincipal();
            if (principal instanceof UserSec user) {
                return user;
            }
        }

        // Caso: proceso interno / scheduler
        return getSystemUser();
    }

    /**
     * Retorna el usuario SYSTEM para procesos internos o cuando no hay usuario autenticado.
     */
    public UserSec getSystemUser() {
        return userRepository.findByIdAndEnabledTrue(SystemUserId.SYSTEM.getId())
                .orElseThrow(() -> new NotFoundException(
                        "userService.getById.error.user",
                        null,
                        "userService.getById.error.log",
                        new Object[]{SystemUserId.SYSTEM, "UserService", "getByIdInternal"},
                        LogLevel.ERROR
                ));
    }
}