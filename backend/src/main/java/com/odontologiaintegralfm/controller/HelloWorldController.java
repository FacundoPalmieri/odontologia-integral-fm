package com.odontologiaintegralfm.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("denyAll()")
public class HelloWorldController {

    @GetMapping("/holaseg")
    //cambiamos hasAuthority por "hasRole"
    //@PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public String secHelloWorld() {
        return "Hola Mundo CON seguridad";
    }

    @GetMapping("/holanoseg")
    @PreAuthorize("permitAll()")
    public String noSecHelloWorld() {
        return "Hola mundo SIN seguridad";
    }

}
