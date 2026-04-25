package com.odontologiaintegralfm.infrastructure.auditing;


import com.odontologiaintegralfm.feature.user.model.UserSec;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<UserSec> {

    @Override
    public Optional<UserSec> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserSec user) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}