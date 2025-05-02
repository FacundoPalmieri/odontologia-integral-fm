package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.dto.PatientCreateResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IPatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author [Facundo Palmieri]
 */

@RestController
@PreAuthorize("denyAll()")
@RequestMapping("/api/patient")
public class PatientController {


    @Autowired
    private IPatientService patientService;

    @PostMapping
    @OnlyAdmistratorAndSecretary
    public ResponseEntity<Response<PatientCreateResponseDTO>> createPatient (@Valid @RequestBody PatientCreateRequestDTO patientCreateRequestDTO) {
        Response<PatientCreateResponseDTO> response = patientService.create(patientCreateRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


}
