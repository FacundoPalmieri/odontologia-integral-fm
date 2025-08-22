package com.odontologiaintegralfm.configuration.securityConfig;

import com.odontologiaintegralfm.model.UserSec;
import com.odontologiaintegralfm.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Componente para obtener el usuario autenticado.
 * Se utiliza para completar la información de auditoría.
 */

@Component
public class AuthenticatedUserService {

    @Autowired
    private IUserRepository userRepository;

    public UserSec  getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username;
        if (authentication == null || !authentication.isAuthenticated()) {
            username = "No autenticado";
        } else {
            username = authentication.getName();
        }

        return userRepository.findUserEntityByUsername(username)
                .orElseGet(() -> {
                    UserSec anon = new UserSec();
                    anon.setUsername(username);
                    return anon;
                });

    }

}
