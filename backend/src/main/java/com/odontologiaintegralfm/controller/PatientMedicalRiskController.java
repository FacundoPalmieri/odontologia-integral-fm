package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessPatientsUpdate;
import com.odontologiaintegralfm.dto.PatientMedicalRiskRequestDTO;
import com.odontologiaintegralfm.dto.PatientMedicalRiskResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.MessageService;
import com.odontologiaintegralfm.service.interfaces.IPatientMedicalRiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/patient/{id}/medical-risk")
public class PatientMedicalRiskController {
    @Autowired
    IPatientMedicalRiskService patientMedicalRiskService;
    @Autowired
    private MessageService messageService;


    /**
     * Crea o actualiza la lista de riesgos médicos de un paciente.
     * <p>Requiere rol <b>Administrador y Secretaria</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista de riesgos médicos de un paciente recuperado exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Actualiza la lista de riesgos médicos de un paciente", description = "Actualiza la lista de riesgos médicos de un paciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Riesgos médicos actualizados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping
    @OnlyAccessPatientsUpdate
    public ResponseEntity<Response<Set<PatientMedicalRiskResponseDTO>>> update (@PathVariable("id") Long id,
                                                                                @RequestBody Set<PatientMedicalRiskRequestDTO> patientMedicalRiskRequestDTO) {

       Set<PatientMedicalRiskResponseDTO> patientMedicalRiskResponseDTOS = patientMedicalRiskService.CreateOrUpdate(id,patientMedicalRiskRequestDTO);

       String messageUser = messageService.getMessage("patientMedicalRiskController.createOrUpdate.user.ok", null, LocaleContextHolder.getLocale());
       return new ResponseEntity<>(new Response<>(true,messageUser, patientMedicalRiskResponseDTOS), HttpStatus.OK);
    }


}
