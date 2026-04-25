package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationUpdate;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.ICreateDentistCalendarLockUseCase;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.IUpdateDentistCalendarLockUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para relación de Dentista con un evento que bloquea el calendario.
 */

@RestController
@RequestMapping("/api/dentist-calendar-lock")
@Validated
public class DentistCalendarLockController {

    private final IUpdateDentistCalendarLockUseCase dentistCalendarLockUpdateService;
    private final ICreateDentistCalendarLockUseCase dentistCalendarLockCreateUseCase;

    public DentistCalendarLockController(ICreateDentistCalendarLockUseCase dentistCalendarLockCreateUseCase, IUpdateDentistCalendarLockUseCase dentistCalendarLockUpdateService) {
        this.dentistCalendarLockCreateUseCase = dentistCalendarLockCreateUseCase;
        this.dentistCalendarLockUpdateService = dentistCalendarLockUpdateService;
    }


    @Operation(summary = "Crear bloqueo de Agenda", description = "Permite crear un bloqueo de agenda para un dentista.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bloqueo creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idPerson}")
    @OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationCreate
    public ResponseEntity<Response<DentistCalendarLockResponseDTO>>create(@PathVariable("idPerson")Long idPerson,
                                                                          @Valid @RequestBody DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {


        Response<DentistCalendarLockResponseDTO> response = dentistCalendarLockCreateUseCase.execute(idPerson, dentistCalendarLockRequestCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }




    @Operation(summary = "Visualizar posibles conflictos por creación de bloqueo de calendario.", description = "Permite visualizar lo posibles conflictos en turnos que puedan surgir por creación de bloqueo de calendario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preview obtenido exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Dentista no encontrado.")
    })
    @PostMapping("/{idPerson}/preview")
    @OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationCreate
    public ResponseEntity<Response<DentistCalendarLockResponseDTO>>createPreview(@PathVariable("idPerson")Long idPerson,
                                                                                 @Valid @RequestBody DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {


        Response<DentistCalendarLockResponseDTO> response = dentistCalendarLockCreateUseCase.executePreview(idPerson, dentistCalendarLockRequestCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }



    @Operation(summary = "Finalizar bloqueo", description = "Actualiza la fecha de fin de bloqueo al momento en que se envía la request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bloqueo finalizado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{idPerson}")
    @OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationUpdate
    public ResponseEntity<Response<DentistCalendarLockResponseDTO>> update(@PathVariable("idPerson")Long idPerson,
                                                                           @Valid @RequestBody DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO) {

        Response<DentistCalendarLockResponseDTO> response = dentistCalendarLockUpdateService.execute(dentistCalendarLockRequestUpdateDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }



}
