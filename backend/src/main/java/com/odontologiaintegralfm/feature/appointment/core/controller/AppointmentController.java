package com.odontologiaintegralfm.feature.appointment.core.controller;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementUpdate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessPersonProfileAndConsultationOrAppointmentsManagementUpdate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead;
import com.odontologiaintegralfm.feature.appointment.core.dto.*;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentConflictService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 *  Controlador de turnos
 */

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private IAppointmentService appointmentService;

    @Autowired
    private IAppointmentConflictService appointmentConflictService;

    @Operation(summary = "Listar turnos en conflicto por dentista", description = "Lista los turnos en conflictos por Id de dentista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/conflict/all/{idDentist}")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead
    public ResponseEntity<Response<List<AppointmentConflictResponseDTO>>> getConflictAll(@PathVariable Long idDentist){
        Response<List<AppointmentConflictResponseDTO>> response = appointmentConflictService.getConflict(idDentist);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Crear turno", description = "Crea un turno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping()
    @OnlyAccessAppointmentsManagementCreate
    public ResponseEntity<Response<AppointmentResponseDTO>> create(@RequestBody @Valid AppointmentCreateRequestDTO appointmentCreateRequestDTO){
        Response<AppointmentResponseDTO> response = appointmentService.create(appointmentCreateRequestDTO);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Reprogramar turno", description = "Reprograma un turno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno reprogramado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Error al reprogramar por validaciones de negocio."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("{idAppointment}/reschedule")
    @OnlyAccessAppointmentsManagementUpdate
    public ResponseEntity<Response<AppointmentResponseDTO>> reschedule(@PathVariable @NotNull Long idAppointment,
                                                                       @RequestBody @Valid AppointmentRescheduleRequestDTO appointmentRescheduleRequestDTO){

        Response<AppointmentResponseDTO> response = appointmentService.reschedule(idAppointment, appointmentRescheduleRequestDTO);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Cancelar turno", description = "Cancelar un turno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno Cancelado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Error al cancelar por validaciones de negocio."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{idAppointment}/cancel")
    @OnlyAccessAppointmentsManagementUpdate
    public ResponseEntity<Response<AppointmentResponseDTO>> cancel(@PathVariable @NotNull Long idAppointment,
                                                                   @RequestBody @Valid AppointmentCancelRequestDTO appointmentCancelRequestDTO) {

        Response<AppointmentResponseDTO> response = appointmentService.cancel(idAppointment, appointmentCancelRequestDTO);
        return ResponseEntity.ok(response);

    }


    @Operation(summary = "Cancelar turnos", description = "Cancelar todos los turnos para una fecha.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turnos Cancelados exitosamente."),
            @ApiResponse(responseCode = "400", description = "Error al cancelar por validaciones de negocio."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{idDentist}/all/cancel")
    @OnlyAccessPersonProfileAndConsultationOrAppointmentsManagementUpdate
    public ResponseEntity<Response<Integer>> cancelAllByDate(@PathVariable @NotNull Long idDentist,
                                                             @RequestParam @NotNull LocalDate date,
                                                             @RequestBody @Valid AppointmentCancelRequestDTO appointmentCancelRequestDTO) {

        Response<Integer> response = appointmentService.cancelAllByDate(idDentist, date, appointmentCancelRequestDTO);
        return ResponseEntity.ok(response);

    }


}
