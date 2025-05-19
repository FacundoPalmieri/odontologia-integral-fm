package com.odontologiaintegralfm.configuration.securityConfig;

import com.odontologiaintegralfm.model.UserSec;
import com.odontologiaintegralfm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author [Facundo Palmieri]
 */
@Component
public class AuthenticatedUserService {
    @Autowired
    private UserService userService;


    public UserSec  getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.getByUsername(authentication.getName());
    }

}
