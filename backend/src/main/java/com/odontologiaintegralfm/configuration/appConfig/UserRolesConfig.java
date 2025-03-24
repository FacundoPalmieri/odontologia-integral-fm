package com.odontologiaintegralfm.configuration.appConfig;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class UserRolesConfig {

    @Value("${custom.roles.odontologo}")
    private String odontologoRole;

    @Value("${custom.roles.secretaria}")
    private String secretariaRole;

    @Value("${custom.roles.adminstrador}")
    private String administradorRole;

    @Value("${custom.roles.desarrollador}")
    private String desarrolladorRole;

}