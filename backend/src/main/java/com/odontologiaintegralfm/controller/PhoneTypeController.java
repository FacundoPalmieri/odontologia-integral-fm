package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.PhoneType;
import com.odontologiaintegralfm.service.interfaces.IPhoneTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/phone-type")
@OnlyAdmistratorAndSecretary
public class PhoneTypeController {

    @Autowired
    private IPhoneTypeService phoneTypeService;

    public ResponseEntity<Response<Set<PhoneType>>> PhoneTypeController() {
        Response<Set<PhoneType>>response = phoneTypeService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
