package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdministrator;
import com.odontologiaintegralfm.dto.MedicalRiskResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.MedicalRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@OnlyAdministrator
@RequestMapping("/medical-risk")
public class MedicalRiskController {
    @Autowired
    private MedicalRiskService medicalRiskService;

    public ResponseEntity<Response<Set<MedicalRiskResponseDTO>>> getAll(){
        Response<Set<MedicalRiskResponseDTO>> response = medicalRiskService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
