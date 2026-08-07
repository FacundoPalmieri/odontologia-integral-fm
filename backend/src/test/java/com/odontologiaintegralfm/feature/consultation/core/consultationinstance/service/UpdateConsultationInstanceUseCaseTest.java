package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationTypePrice;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypePriceService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypeService;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service.PromotionQueryService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.TreatmentCondition;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentConditionService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.ConsultationEventService;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceBasicResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.repository.IOdontogramRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service.PrestationStepInstanceQueryService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.person.core.model.Person;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces.ISystemParameterService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * CASO: Suite completa de UpdateConsultationInstanceUseCase (REQ-021).
 * Edición in-place (ADR-0008 reescrito): Odontogram/PrestationInstance/PrestationStepInstance se editan
 * actualizando el registro persistido directamente, mismo id. Solo el alta inserta; solo la baja explícita
 * usa disable().
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UpdateConsultationInstanceUseCaseTest {

    @Mock private ConsultationInstanceQueryService consultationInstanceQueryService;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private ISystemParameterService systemParameterService;
    @Mock private IConsultationInstanceRepository consultationInstanceRepository;
    @Mock private IOdontogramRepository odontogramRepository;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private PrestationInstanceDomainService prestationInstanceDomainService;
    @Mock private PrestationTypeService prestationTypeService;
    @Mock private PrestationTypePriceService prestationTypePriceService;
    @Mock private PrestationStepService prestationStepService;
    @Mock private IPrestationStepInstanceRepository prestationStepInstanceRepository;
    @Mock private TreatmentService treatmentService;
    @Mock private TreatmentConditionService treatmentConditionService;
    @Mock private PromotionQueryService promotionQueryService;
    @Mock private MessageSource messageSource;
    @Mock private ConsultationEventService consultationEventService;
    @Mock private ConsultationMapper consultationMapper;

    @InjectMocks private UpdateConsultationInstanceUseCase useCase;

    private static final Long INSTANCE_ID = 1L;
    private static final String CORRECTIONS_HOURS = "24";
    private static final long CORRECTIONS_HOURS_LONG = 24L;

    // ─── Helpers de arrange comunes ───────────────────────────────────────────

    private ConsultationInstanceUpdateRequestDTO emptyDto() {
        return new ConsultationInstanceUpdateRequestDTO(null, List.of(), List.of(), List.of(), List.of());
    }

    private ConsultationInstanceUpdateRequestDTO dto(String observation,
                                                      List<OdontogramUpdateRequestDTO> odontogram,
                                                      List<PrestationInstanceUpdateRequestDTO> prestationExisting,
                                                      List<PrestationStepUpdateRequestDTO> stepAdvancements,
                                                      List<PrestationInstanceRequestDTO> prestationNew) {
        return new ConsultationInstanceUpdateRequestDTO(observation, odontogram, prestationExisting, stepAdvancements, prestationNew);
    }

    private Dentist dentistOwner(Long personId) {
        Person dentistPerson = mock(Person.class);
        when(dentistPerson.getId()).thenReturn(personId);
        Dentist dentist = mock(Dentist.class);
        when(dentist.getPerson()).thenReturn(dentistPerson);
        return dentist;
    }

    private UserSec authenticatedDentistUser(Long personId) {
        Person authenticatedPerson = mock(Person.class);
        when(authenticatedPerson.getId()).thenReturn(personId);
        UserSec user = mock(UserSec.class);
        when(user.getPerson()).thenReturn(authenticatedPerson);
        return user;
    }

    /** Arranca instancia+consulta+dentist+paciente dentro de ventana, y stubea lo mínimo para llegar a ejecutar el flujo completo. */
    private ConsultationInstance arrangeBaseWithinWindow(Long patientId) {
        Dentist dentist = dentistOwner(10L);
        Patient patient = mock(Patient.class);
        if (patientId != null) lenient().when(patient.getId()).thenReturn(patientId);
        Consultation consultation = mock(Consultation.class);
        lenient().when(consultation.getDentist()).thenReturn(dentist);
        lenient().when(consultation.getPatient()).thenReturn(patient);

        ConsultationInstance instance = mock(ConsultationInstance.class);
        lenient().when(instance.getId()).thenReturn(INSTANCE_ID);
        when(instance.getConsultation()).thenReturn(consultation);
        when(instance.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(1));

        when(consultationInstanceQueryService.findById(INSTANCE_ID)).thenReturn(instance);
        UserSec authenticatedUser = authenticatedDentistUser(10L);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        lenient().when(systemParameterService.getByKey(SystemParameterKey.CONSULTATION_INSTANCE_CORRECTIONS)).thenReturn(CORRECTIONS_HOURS);
        return instance;
    }

    /** Stubs neutros para que buildResponse() no explote una vez que execute() llega al final feliz. */
    private void arrangeHappyResponseStubs(Consultation consultation) {
        when(consultationMapper.toDTOWithoutPublish(consultation)).thenReturn(mock(com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO.class));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");
    }

    // ─── § 1/2: Existencia + ownership (404) ──────────────────────────────────

    /**
     * CASO: La ConsultationInstance no existe.
     * Validación: Lanza NotFoundException, 404; ningún side-effect posterior.
     */
    @Test
    void execute_consultaNotFound_return404() {
        when(consultationInstanceQueryService.findById(INSTANCE_ID))
                .thenThrow(new NotFoundException("exception.getConsultationInstanceUseCase.notFound.user", null,
                        "exception.getConsultationInstanceUseCase.notFound.log", new Object[]{INSTANCE_ID}, null));

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, emptyDto()))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(odontogramRepository, prestationInstanceRepository, consultationEventService);
    }

    /**
     * CASO: La instancia existe pero dentist.person.id != authenticatedUser.person.id.
     * Validación: Lanza NotFoundException (no ForbiddenException — ADR-0027).
     */
    @Test
    void execute_consultaNotOwnedByDentist_return404() {
        Dentist dentist = dentistOwner(10L);
        Consultation consultation = mock(Consultation.class);
        when(consultation.getDentist()).thenReturn(dentist);
        ConsultationInstance instance = mock(ConsultationInstance.class);
        when(instance.getConsultation()).thenReturn(consultation);

        when(consultationInstanceQueryService.findById(INSTANCE_ID)).thenReturn(instance);
        UserSec authenticatedUser = authenticatedDentistUser(999L);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, emptyDto()))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(odontogramRepository, prestationInstanceRepository, consultationEventService);
    }

    // ─── § 2: Ventana de corrección del contenedor (409) ──────────────────────

    /**
     * CASO: now() posterior a createdAt + CONSULTATION_INSTANCE_CORRECTIONS horas.
     * Validación: Lanza ConflictException, 409; ninguna escritura posterior.
     */
    @Test
    void execute_deadlineExceeded_return409() {
        Dentist dentist = dentistOwner(10L);
        Consultation consultation = mock(Consultation.class);
        when(consultation.getDentist()).thenReturn(dentist);
        ConsultationInstance instance = mock(ConsultationInstance.class);
        when(instance.getConsultation()).thenReturn(consultation);
        when(instance.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(48));

        when(consultationInstanceQueryService.findById(INSTANCE_ID)).thenReturn(instance);
        UserSec authenticatedUser = authenticatedDentistUser(10L);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(systemParameterService.getByKey(SystemParameterKey.CONSULTATION_INSTANCE_CORRECTIONS)).thenReturn(CORRECTIONS_HOURS);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, emptyDto()))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(odontogramRepository, prestationInstanceRepository, consultationEventService);
    }

    /**
     * CASO: now() anterior al deadline, request mínima (sin sub-recursos).
     * Validación: No lanza excepción de ventana, el flujo continúa hasta el final.
     */
    @Test
    void execute_withinDeadline_proceeds() {
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        Response<ConsultationInstanceBasicResponseDTO> result = useCase.execute(INSTANCE_ID, emptyDto());

        assertThat(result).isNotNull();
        verify(consultationInstanceQueryService).findById(INSTANCE_ID);
    }

    // ─── § 3 regla #3/#4: Odontograma alta / edición in-place ─────────────────

    /**
     * CASO: Elemento de odontogram con id=null, enabled=true.
     * Validación: Se inserta un Odontogram nuevo (saveAll invocado con elemento sin id); nada se deshabilita.
     */
    @Test
    void execute_odontogramNew_insertsWithoutDisable() {
        OdontogramUpdateRequestDTO newOdontogram = new OdontogramUpdateRequestDTO(null, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(treatmentService.findById(1L)).thenReturn(mock(Treatment.class));
        when(treatmentConditionService.findById(1L)).thenReturn(mock(TreatmentCondition.class));
        when(odontogramRepository.saveAll(any())).thenReturn(List.of(mock(Odontogram.class)));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(newOdontogram), List.of(), List.of(), List.of()));

        ArgumentCaptor<List<Odontogram>> captor = ArgumentCaptor.forClass(List.class);
        verify(odontogramRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        verify(odontogramRepository, never()).save(any());
    }

    /**
     * CASO: Elemento con id existente, enabled=true, treatmentId distinto al persistido.
     * Validación: El registro persistido se actualiza IN-PLACE (mismo id); NO se invoca disable(), NO se
     * invoca saveAll con elemento nuevo — el mock del Odontogram persistido recibe setTreatment(...) con el
     * valor corregido.
     */
    @Test
    void execute_odontogramEdited_updatesInPlace() {
        OdontogramUpdateRequestDTO edited = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 2L, 1L, true);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existing = mock(Odontogram.class);
        when(existing.getId()).thenReturn(50L);
        when(existing.getTooth()).thenReturn(Tooth.T11);
        when(existing.getToothFace()).thenReturn(ToothFace.TOP);
        Treatment persistedTreatment = mock(Treatment.class);
        when(persistedTreatment.getId()).thenReturn(1L);
        when(existing.getTreatment()).thenReturn(persistedTreatment);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existing));
        Treatment newTreatment = mock(Treatment.class);
        when(treatmentService.findById(2L)).thenReturn(newTreatment);
        TreatmentCondition sameCondition = mock(TreatmentCondition.class);
        when(treatmentConditionService.findById(1L)).thenReturn(sameCondition);
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(edited), List.of(), List.of(), List.of()));

        verify(existing, never()).disable(any());
        verify(odontogramRepository, never()).saveAll(any());
        verify(existing).setTreatment(newTreatment);
    }

    /**
     * CASO: Elemento con id existente, enabled=true, todos los campos iguales a los persistidos.
     * Validación: No se dispara ningún setter/save sobre el registro; se satisface la completitud igual, sin error.
     */
    @Test
    void execute_odontogramEditedWithNoFieldChanges_noUpdateDispatched() {
        OdontogramUpdateRequestDTO unchanged = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existing = mock(Odontogram.class);
        when(existing.getId()).thenReturn(50L);
        when(existing.getTooth()).thenReturn(Tooth.T11);
        when(existing.getToothFace()).thenReturn(ToothFace.TOP);
        Treatment persistedTreatment = mock(Treatment.class);
        when(persistedTreatment.getId()).thenReturn(1L);
        when(existing.getTreatment()).thenReturn(persistedTreatment);
        TreatmentCondition persistedCondition = mock(TreatmentCondition.class);
        when(persistedCondition.getId()).thenReturn(1L);
        when(existing.getTreatmentCondition()).thenReturn(persistedCondition);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existing));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(unchanged), List.of(), List.of(), List.of()));

        verify(existing, never()).disable(any());
        verify(existing, never()).setTooth(any());
        verify(existing, never()).setToothFace(any());
        verify(existing, never()).setTreatment(any());
        verify(existing, never()).setTreatmentCondition(any());
        verify(odontogramRepository, never()).saveAll(any());
        verify(treatmentService, never()).findById(any());
    }

    /**
     * CASO: Elemento con id existente, enabled=true, tooth/toothFace distinto al persistido; existe una
     * PrestationInstance viva (con step COMPLETED) cuyo odontogram es este mismo registro, ubicación nueva
     * compatible con allowedScopes de esa prestación, declarada en dto.prestationExisting() con enabled:true
     * sin cambios propios (regla #6, sin excepción).
     * Validación: El Odontogram se actualiza in-place; la PrestationInstance NO cambia status/enabled/steps —
     * sigue apuntando al mismo id, sin invocación de re-vinculación (setOdontogram nunca se llama).
     */
    @Test
    void execute_odontogramEditedWithLinkedPrestation_keepsSameOdontogramId() {
        OdontogramUpdateRequestDTO edited = new OdontogramUpdateRequestDTO(50L, Tooth.T21, ToothFace.TOP, 1L, 1L, true);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);
        when(existingOdontogram.getTooth()).thenReturn(Tooth.T11);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getId()).thenReturn(200L);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);
        PrestationType linkedPrestationType = mock(PrestationType.class);
        when(linkedPrestationType.getId()).thenReturn(10L);
        when(linkedPrestation.getType()).thenReturn(linkedPrestationType);

        PrestationStepInstance completedStep = mock(PrestationStepInstance.class);

        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(treatmentService.findById(1L)).thenReturn(mock(Treatment.class));
        when(treatmentConditionService.findById(1L)).thenReturn(mock(TreatmentCondition.class));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(linkedPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(linkedPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of());
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(edited), List.of(keepPrestation), List.of(), List.of()));

        verify(existingOdontogram, never()).disable(any());
        verify(existingOdontogram).setTooth(Tooth.T21);
        verify(prestationInstanceDomainService).validateScopeAllowedForType(linkedPrestationType, PrestationScopeType.TOOTH_FACE);
        verify(linkedPrestation, never()).setOdontogram(any());
        verify(linkedPrestation, never()).disable(any());
        verify(completedStep, never()).disable(any());
    }

    /**
     * CASO: Misma precondición que el test anterior, pero la ubicación nueva del Odontogram NO es compatible
     * con allowedScopes del PrestationType de la prestación vinculada.
     * Validación: Lanza ConflictException, 409 (validateScopeAllowedForType) — el cambio no se aplica.
     */
    @Test
    void execute_odontogramEditedWithLinkedPrestationScopeNotAllowed_return409() {
        OdontogramUpdateRequestDTO edited = new OdontogramUpdateRequestDTO(50L, Tooth.T21, ToothFace.TOP, 1L, 1L, true);
        arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);
        when(existingOdontogram.getTooth()).thenReturn(Tooth.T11);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);
        PrestationType linkedPrestationType = mock(PrestationType.class);
        when(linkedPrestation.getType()).thenReturn(linkedPrestationType);

        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));

        doThrow(new ConflictException("exception.prestationInstanceDomainService.validateScopeAllowedForType.user", null,
                "exception.prestationInstanceDomainService.validateScopeAllowedForType.log", new Object[]{PrestationScopeType.TOOTH_FACE}, null))
                .when(prestationInstanceDomainService).validateScopeAllowedForType(linkedPrestationType, PrestationScopeType.TOOTH_FACE);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(edited), List.of(keepPrestation), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(existingOdontogram, never()).setTooth(any());
        verify(existingOdontogram, never()).disable(any());
    }

    /**
     * CASO: Elemento con id existente, enabled=false.
     * Validación: El registro persistido queda disable(); no se inserta ningún registro nuevo.
     */
    @Test
    void execute_odontogramDisabledExplicit_disablesOnly() {
        OdontogramUpdateRequestDTO disabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existing = mock(Odontogram.class);
        when(existing.getId()).thenReturn(50L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existing));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(disabled), List.of(), List.of(), List.of()));

        verify(existing).disable(any());
        verify(odontogramRepository, never()).saveAll(any());
        verify(treatmentService, never()).findById(any());
    }

    /**
     * CASO: Elemento con id existente, enabled=false, y además treatmentId distinto al persistido en el mismo elemento.
     * Validación: El registro queda disable(); el cambio de treatmentId se ignora sin error, no se inserta nada.
     */
    @Test
    void execute_odontogramDisabledExplicitWithOtherFieldsChanged_ignoresOtherFields() {
        OdontogramUpdateRequestDTO disabledWithChange = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 99L, 1L, false);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existing = mock(Odontogram.class);
        when(existing.getId()).thenReturn(50L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existing));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(disabledWithChange), List.of(), List.of(), List.of()));

        verify(existing).disable(any());
        verify(odontogramRepository, never()).saveAll(any());
        verify(treatmentService, never()).findById(99L);
    }

    /**
     * CASO: Odontogram vigente persistido cuyo id no aparece en absoluto en dto.odontogram().
     * Validación: Lanza ConflictException, 409, "objeto incompleto".
     */
    @Test
    void execute_odontogramMissingFromRequest_return409() {
        arrangeBaseWithinWindow(1L);

        Odontogram existing = mock(Odontogram.class);
        when(existing.getId()).thenReturn(50L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, emptyDto()))
                .isInstanceOf(ConflictException.class);

        verify(existing, never()).disable(any());
        verify(odontogramRepository, never()).saveAll(any());
    }

    /**
     * CASO: Elemento con enabled=false e id que no matchea ningún Odontogram persistido de esta instancia.
     * Validación: Lanza NotFoundException, 404.
     */
    @Test
    void execute_odontogramIdNotFound_return404() {
        OdontogramUpdateRequestDTO unknownId = new OdontogramUpdateRequestDTO(999L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        arrangeBaseWithinWindow(1L);

        Odontogram existing = mock(Odontogram.class);
        when(existing.getId()).thenReturn(50L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(unknownId), List.of(), List.of(), List.of())))
                .isInstanceOf(NotFoundException.class);

        verify(existing, never()).disable(any());
        verify(odontogramRepository, never()).saveAll(any());
    }

    // ─── § 3 regla #8: Cascada Odontogram → PrestationInstance ────────────────

    /**
     * CASO: Odontogram dado de baja (enabled:false) referenciado por una PrestationInstance viva con todos
     * sus steps dentro de ventana (incluido alguno COMPLETED).
     * Validación: La PrestationInstance y todos sus PrestationStepInstance no deshabilitados quedan disable()
     * en la misma transacción, sin bloqueo por COMPLETED.
     */
    @Test
    void execute_odontogramWithLinkedPrestation_cascadeDisablesPrestation() {
        OdontogramUpdateRequestDTO disabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getId()).thenReturn(200L);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);

        PrestationStepInstance completedStep = mock(PrestationStepInstance.class);
        PrestationStepInstance inProgressStep = mock(PrestationStepInstance.class);

        PrestationInstanceUpdateRequestDTO declaredDisabled =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(linkedPrestation))
                .thenReturn(List.of(completedStep, inProgressStep));
        when(prestationInstanceDomainService.isStepEditable(completedStep, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(inProgressStep, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(disabled), List.of(declaredDisabled), List.of(), List.of()));

        verify(existingOdontogram).disable(any());
        verify(linkedPrestation).disable(any());
        verify(completedStep).disable(any());
        verify(inProgressStep).disable(any());
    }

    /**
     * CASO: Odontogram dado de baja con PrestationInstance viva vinculada (cascada), y el front referencia
     * esa prestación en prestationExisting con enabled:true.
     * Validación: Lanza ConflictException, 409 — contradice el estado que la cascada ya aplicó.
     */
    @Test
    void execute_odontogramWithLinkedPrestationDeclaredEnabled_return409() {
        OdontogramUpdateRequestDTO disabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getId()).thenReturn(200L);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);

        PrestationInstanceUpdateRequestDTO contradictoryKeep =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(linkedPrestation)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(disabled), List.of(contradictoryKeep), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(linkedPrestation, times(1)).disable(any());
    }

    /**
     * CASO: Odontogram dado de baja con PrestationInstance viva vinculada (cascada), ausente en absoluto
     * de prestationExisting.
     * Validación: Lanza ConflictException, 409 — objeto incompleto, sin excepción para este caso (el front
     * puede identificar la prestación por PrestationInstanceResponseDTO.odontogram.id, expuesto en el GET).
     */
    @Test
    void execute_odontogramWithLinkedPrestationAbsentFromRequest_return409() {
        OdontogramUpdateRequestDTO disabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getId()).thenReturn(200L);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(linkedPrestation)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(disabled), List.of(), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(linkedPrestation, times(1)).disable(any());
    }

    /**
     * CASO: Odontograma dado de baja vinculado a una prestación con un step COMPLETED DENTRO de su propia
     * ventana (nada más fuera).
     * Validación: No lanza excepción — COMPLETED ya no bloquea por sí solo (§ 3.5 revisión, regla #11).
     */
    @Test
    void execute_cascadeWithCompletedStepWithinWindow_disableSuccess() {
        OdontogramUpdateRequestDTO disabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getId()).thenReturn(200L);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);

        PrestationStepInstance completedStep = mock(PrestationStepInstance.class);

        PrestationInstanceUpdateRequestDTO declaredDisabled =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(linkedPrestation)).thenReturn(List.of(completedStep));
        when(prestationInstanceDomainService.isStepEditable(completedStep, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(disabled), List.of(declaredDisabled), List.of(), List.of()));

        verify(linkedPrestation).disable(any());
        verify(completedStep).disable(any());
    }

    /**
     * CASO: Odontograma dado de baja vinculado a una prestación con un step (cualquier status) cuya
     * ConsultationInstance de origen superó su propia ventana.
     * Validación: Lanza ConflictException, 409 — la cascada respeta la guarda de ventana.
     */
    @Test
    void execute_cascadeWithStepOutOfWindow_return409() {
        OdontogramUpdateRequestDTO disabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getId()).thenReturn(200L);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);

        PrestationStepInstance stepOutOfWindow = mock(PrestationStepInstance.class);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(linkedPrestation)).thenReturn(List.of(stepOutOfWindow));
        when(prestationInstanceDomainService.isStepEditable(stepOutOfWindow, CORRECTIONS_HOURS_LONG)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(disabled), List.of(), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(linkedPrestation, never()).disable(any());
        verify(stepOutOfWindow, never()).disable(any());
    }

    /**
     * CASO: PrestationInstance dada de baja explícitamente, con Odontogram asociado.
     * Validación: El Odontogram asociado permanece enabled=true, sin invocación de disable() sobre él.
     */
    @Test
    void execute_prestationCancelled_odontogramNotAffected() {
        Odontogram linkedOdontogram = mock(Odontogram.class);
        when(linkedOdontogram.getId()).thenReturn(50L);
        when(linkedOdontogram.getTooth()).thenReturn(Tooth.T11);
        when(linkedOdontogram.getToothFace()).thenReturn(ToothFace.TOP);
        Treatment persistedTreatment = mock(Treatment.class);
        when(persistedTreatment.getId()).thenReturn(1L);
        when(linkedOdontogram.getTreatment()).thenReturn(persistedTreatment);
        TreatmentCondition persistedTreatmentCondition = mock(TreatmentCondition.class);
        when(persistedTreatmentCondition.getId()).thenReturn(1L);
        when(linkedOdontogram.getTreatmentCondition()).thenReturn(persistedTreatmentCondition);

        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(linkedOdontogram);

        OdontogramUpdateRequestDTO keepOdontogram = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        TreatmentCondition requiredCondition = mock(TreatmentCondition.class);
        when(treatmentConditionService.findByName(TreatmentConditionType.REQUIRED.name())).thenReturn(requiredCondition);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(keepOdontogram), List.of(disablePrestation), List.of(), List.of()));

        verify(existingPrestation).disable(any());
        verify(linkedOdontogram, never()).disable(any());
    }

    // ─── § 3 regla #9: Baja explícita de prestación completa ──────────────────

    /**
     * CASO: prestationExisting con enabled:false para una prestación viva con 2 steps IN_PROGRESS/OMITTED,
     * declarando ambos steps en stepAdvancements con enabled:false (regla #6/#9, sin excepción).
     * Validación: La prestación y ambos steps quedan disable().
     */
    @Test
    void execute_prestationInstanceDisabledExplicit_disablesWithSteps() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);

        PrestationStepInstance stepInProgress = mock(PrestationStepInstance.class);
        when(stepInProgress.getId()).thenReturn(300L);
        PrestationStepInstance stepOmitted = mock(PrestationStepInstance.class);
        when(stepOmitted.getId()).thenReturn(301L);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);
        PrestationStepUpdateRequestDTO disableStep1 = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
        PrestationStepUpdateRequestDTO disableStep2 = new PrestationStepUpdateRequestDTO(200L, 301L, null, null, false);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation))
                .thenReturn(List.of(stepInProgress, stepOmitted));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepInProgress, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepOmitted, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(disableStep1, disableStep2), List.of()));

        verify(existingPrestation).disable(any());
        verify(stepInProgress).disable(any());
        verify(stepOmitted).disable(any());
    }

    /**
     * CASO: Baja explícita de prestación completa con un step COMPLETED DENTRO de su propia ventana
     * (nada más fuera).
     * Validación: No lanza excepción; prestación y steps quedan disable() — COMPLETED ya no bloquea por sí solo.
     */
    @Test
    void execute_prestationInstanceDisabledExplicitWithCompletedStepWithinWindow_disableSuccess() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);

        PrestationStepInstance completedStep = mock(PrestationStepInstance.class);
        when(completedStep.getId()).thenReturn(300L);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);
        PrestationStepUpdateRequestDTO disableStep = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of(completedStep));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(completedStep, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(disableStep), List.of()));

        verify(existingPrestation).disable(any());
        verify(completedStep).disable(any());
    }

    /**
     * CASO: Baja explícita de prestación completa con 2 steps COMPLETED: uno dentro de su propia ventana,
     * otro fuera.
     * Validación: Lanza ConflictException, 409 — bloqueado por el step fuera de ventana, no por ser COMPLETED.
     */
    @Test
    void execute_prestationInstanceDisabledExplicitWithTwoCompletedStepsOneOutOfWindow_return409() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);

        PrestationStepInstance completedWithinWindow = mock(PrestationStepInstance.class);
        when(completedWithinWindow.getId()).thenReturn(300L);
        PrestationStepInstance completedOutOfWindow = mock(PrestationStepInstance.class);
        when(completedOutOfWindow.getId()).thenReturn(301L);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);
        PrestationStepUpdateRequestDTO disableStep1 = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
        PrestationStepUpdateRequestDTO disableStep2 = new PrestationStepUpdateRequestDTO(200L, 301L, null, null, false);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation))
                .thenReturn(List.of(completedWithinWindow, completedOutOfWindow));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(completedWithinWindow, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(completedOutOfWindow, CORRECTIONS_HOURS_LONG)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(disableStep1, disableStep2), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(existingPrestation, never()).disable(any());
        verify(completedWithinWindow, never()).disable(any());
        verify(completedOutOfWindow, never()).disable(any());
    }

    /**
     * CASO: Baja explícita de prestación completa con un step (cualquier status) fuera de su propia ventana.
     * Validación: Lanza ConflictException, 409.
     */
    @Test
    void execute_prestationInstanceDisabledExplicitWithStepOutOfWindow_return409() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);

        PrestationStepInstance stepOutOfWindow = mock(PrestationStepInstance.class);
        when(stepOutOfWindow.getId()).thenReturn(300L);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);
        PrestationStepUpdateRequestDTO disableStep = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of(stepOutOfWindow));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepOutOfWindow, CORRECTIONS_HOURS_LONG)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(disableStep), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(existingPrestation, never()).disable(any());
        verify(stepOutOfWindow, never()).disable(any());
    }

    // ─── § 3 regla #5/#20: Baja individual de step ────────────────────────────

    /**
     * CASO: stepAdvancements con un elemento id existente, enabled:false, dentro de una prestación que sigue
     * viva (otros steps con enabled:true); prestación declarada en prestationExisting con enabled:true sin
     * cambios propios.
     * Validación: Solo ese PrestationStepInstance queda disable(); la PrestationInstance sigue enabled=true.
     */
    @Test
    void execute_stepIndividualDisabledExplicit_disablesOnlyThatStep() {
        PrestationInstance livePrestation = mock(PrestationInstance.class);
        when(livePrestation.getId()).thenReturn(200L);
        PrestationType livePrestationType = mock(PrestationType.class);
        when(livePrestationType.getId()).thenReturn(10L);
        when(livePrestation.getType()).thenReturn(livePrestationType);

        PrestationStep catalogStep1 = mock(PrestationStep.class);
        PrestationStepInstance stepToDisable = mock(PrestationStepInstance.class);
        when(stepToDisable.getId()).thenReturn(300L);

        PrestationStep catalogStep2 = mock(PrestationStep.class);
        when(catalogStep2.getId()).thenReturn(2L);
        PrestationStepInstance stepStillLive = mock(PrestationStepInstance.class);
        when(stepStillLive.getId()).thenReturn(301L);
        when(stepStillLive.getStep()).thenReturn(catalogStep2);

        PrestationStepUpdateRequestDTO disableStepDto = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
        PrestationStepUpdateRequestDTO keepStepDto = new PrestationStepUpdateRequestDTO(200L, 301L, null, PrestationStepStatus.IN_PROGRESS, true);
        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(livePrestation));
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(catalogStep1, catalogStep2));
        when(prestationStepInstanceRepository.findByPrestationInstance(livePrestation))
                .thenReturn(List.of(stepToDisable, stepStillLive));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(livePrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepToDisable, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepStillLive, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(disableStepDto, keepStepDto), List.of()));

        verify(stepToDisable).disable(any());
        verify(stepStillLive, never()).disable(any());
        verify(livePrestation, never()).disable(any());
    }

    /**
     * CASO: Baja explícita (enabled:false) de un step en estado COMPLETED, DENTRO de su propia ventana,
     * prestación viva declarada en prestationExisting sin cambios propios.
     * Validación: No lanza excepción; solo ese step queda disable() — COMPLETED ya no bloquea por sí solo.
     */
    @Test
    void execute_stepIndividualCompletedWithinWindowDisable_success() {
        PrestationInstance livePrestation = mock(PrestationInstance.class);
        when(livePrestation.getId()).thenReturn(200L);
        PrestationType livePrestationType = mock(PrestationType.class);
        when(livePrestationType.getId()).thenReturn(10L);
        when(livePrestation.getType()).thenReturn(livePrestationType);

        PrestationStep catalogStep = mock(PrestationStep.class);
        PrestationStepInstance completedStep = mock(PrestationStepInstance.class);
        when(completedStep.getId()).thenReturn(300L);

        PrestationStepUpdateRequestDTO disableStepDto = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(livePrestation));
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(catalogStep));
        when(prestationStepInstanceRepository.findByPrestationInstance(livePrestation)).thenReturn(List.of(completedStep));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(livePrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(completedStep, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(disableStepDto), List.of()));

        verify(completedStep).disable(any());
    }

    /**
     * CASO: Step (cualquier status, incluido COMPLETED) con enabled:false o status distinto al persistido,
     * cuya ConsultationInstance de origen superó su propia ventana, y el step no está ya enabled=false;
     * prestación declarada en prestationExisting sin cambios propios.
     * Validación: Lanza ConflictException, 409 (ADR-0029).
     */
    @Test
    void execute_stepOutOfWindowDisable_return409() {
        PrestationInstance livePrestation = mock(PrestationInstance.class);
        when(livePrestation.getId()).thenReturn(200L);

        PrestationStepInstance stepOutOfWindow = mock(PrestationStepInstance.class);
        when(stepOutOfWindow.getId()).thenReturn(300L);

        PrestationStepUpdateRequestDTO disableStepDto = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(livePrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(livePrestation)).thenReturn(List.of(stepOutOfWindow));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(livePrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepOutOfWindow, CORRECTIONS_HOURS_LONG)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(disableStepDto), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(stepOutOfWindow, never()).disable(any());
    }

    /**
     * CASO: Step fuera de ventana referenciado con el mismo status y enabled:true que el persistido;
     * prestación declarada sin cambios propios.
     * Validación: No lanza excepción; el step se ignora sin cambios (ADR-0024).
     */
    @Test
    void execute_stepOutOfWindowWithSameValue_ignoredWithoutError() {
        PrestationInstance livePrestation = mock(PrestationInstance.class);
        when(livePrestation.getId()).thenReturn(200L);
        PrestationType livePrestationType = mock(PrestationType.class);
        when(livePrestationType.getId()).thenReturn(10L);
        when(livePrestation.getType()).thenReturn(livePrestationType);

        PrestationStep catalogStep = mock(PrestationStep.class);
        PrestationStepInstance stepOutOfWindow = mock(PrestationStepInstance.class);
        when(stepOutOfWindow.getId()).thenReturn(300L);
        when(stepOutOfWindow.getStatus()).thenReturn(PrestationStepStatus.COMPLETED);

        PrestationStepUpdateRequestDTO sameValueDto = new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.COMPLETED, true);
        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(livePrestation));
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(catalogStep));
        when(prestationStepInstanceRepository.findByPrestationInstance(livePrestation)).thenReturn(List.of(stepOutOfWindow));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(livePrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepOutOfWindow, CORRECTIONS_HOURS_LONG)).thenReturn(false);
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(sameValueDto), List.of()));

        verify(stepOutOfWindow, never()).disable(any());
        ArgumentCaptor<List> editableStepRequestCaptor = ArgumentCaptor.forClass(List.class);
        verify(prestationInstanceDomainService).buildStepAdvancements(any(), any(), editableStepRequestCaptor.capture(), any(), any());
        assertThat(editableStepRequestCaptor.getValue()).isEmpty();
    }

    /**
     * CASO: Baja de una prestación con un step ya enabled=false (histórico) fuera de ventana; el único step
     * vivo (stepStillLive) declarado en stepAdvancements con enabled:false.
     * Validación: No lanza ConflictException — el step terminal (ya deshabilitado, fuera del @Where) no bloquea.
     */
    @Test
    void execute_stepAlreadyDisabledOutOfWindow_noErrorOnPrestationDisable() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);

        PrestationStepInstance stepStillLive = mock(PrestationStepInstance.class);
        when(stepStillLive.getId()).thenReturn(300L);

        // La colección que se itera ya viene filtrada por @Where(clause="enabled = true") —
        // un step ya deshabilitado nunca aparece en findByPrestationInstance.
        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);
        PrestationStepUpdateRequestDTO disableStep = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of(stepStillLive));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepStillLive, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(disableStep), List.of()));

        verify(existingPrestation).disable(any());
        verify(stepStillLive).disable(any());
    }

    // ─── § 3 regla #6: Completitud ────────────────────────────────────────────

    /**
     * CASO: PrestationInstance vigente persistida cuyo id no aparece en absoluto en dto.prestationExisting().
     * Validación: Lanza ConflictException, 409, objeto incompleto.
     */
    @Test
    void execute_prestationInstanceMissingFromRequest_return409() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, emptyDto()))
                .isInstanceOf(ConflictException.class);

        verify(existingPrestation, never()).disable(any());
    }

    /**
     * CASO: Step vigente persistido, prestación padre viva y presente en prestationExisting con enabled:true,
     * step ausente de stepAdvancements.
     * Validación: Lanza ConflictException, 409, objeto incompleto.
     */
    @Test
    void execute_stepMissingFromRequestWithinLivePrestation_return409() {
        PrestationInstance livePrestation = mock(PrestationInstance.class);
        when(livePrestation.getId()).thenReturn(200L);
        when(livePrestation.getOdontogram()).thenReturn(null);

        PrestationStepInstance stepMissing = mock(PrestationStepInstance.class);
        when(stepMissing.getId()).thenReturn(300L);

        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(livePrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(livePrestation)).thenReturn(List.of(stepMissing));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(livePrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(stepMissing, never()).disable(any());
    }

    /**
     * CASO: Prestación viva con 3 steps vigentes; el front declara stepAdvancements con solo 1 de ellos
     * (actividad parcial real) y no declara los otros 2 vigentes.
     * Validación: Lanza ConflictException, 409 — la completitud de steps exige que TODOS los steps vivos de
     * la prestación estén declarados, no solo el que cambió; no hay excepción por actividad parcial.
     */
    @Test
    void execute_stepMissingFromRequestWithPartialActivityOnOtherStep_return409() {
        PrestationInstance livePrestation = mock(PrestationInstance.class);
        when(livePrestation.getId()).thenReturn(200L);
        when(livePrestation.getOdontogram()).thenReturn(null);
        PrestationType livePrestationType = mock(PrestationType.class);
        when(livePrestationType.getId()).thenReturn(10L);
        when(livePrestation.getType()).thenReturn(livePrestationType);

        PrestationStep catalogStep = mock(PrestationStep.class);
        when(catalogStep.getId()).thenReturn(9L);

        PrestationStepInstance stepDeclared = mock(PrestationStepInstance.class);
        when(stepDeclared.getId()).thenReturn(300L);
        when(stepDeclared.getStep()).thenReturn(catalogStep);
        PrestationStepInstance stepNotDeclared1 = mock(PrestationStepInstance.class);
        when(stepNotDeclared1.getId()).thenReturn(301L);
        PrestationStepInstance stepNotDeclared2 = mock(PrestationStepInstance.class);
        when(stepNotDeclared2.getId()).thenReturn(302L);

        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);
        PrestationStepUpdateRequestDTO declaredStep =
                new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.COMPLETED, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(livePrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(livePrestation))
                .thenReturn(List.of(stepDeclared, stepNotDeclared1, stepNotDeclared2));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(livePrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepDeclared, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepNotDeclared1, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepNotDeclared2, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(catalogStep));
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(declaredStep), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(livePrestation, never()).disable(any());
        verify(stepNotDeclared1, never()).disable(any());
        verify(stepNotDeclared2, never()).disable(any());
    }

    /**
     * CASO: Step vigente persistido, ausente de stepAdvancements, y su PrestationInstance padre viene en
     * prestationExisting con enabled:false.
     * Validación: Lanza ConflictException, 409 — la ausencia es contradicción con la baja del padre (regla
     * #6/#9, sin excepción): el front debe declarar cada step vigente con enabled:false explícito.
     */
    @Test
    void execute_stepMissingFromRequestWhenParentPrestationDisabled_return409() {
        PrestationInstance disabledPrestation = mock(PrestationInstance.class);
        when(disabledPrestation.getId()).thenReturn(200L);

        PrestationStepInstance stepMissing = mock(PrestationStepInstance.class);
        when(stepMissing.getId()).thenReturn(300L);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(disabledPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(disabledPrestation)).thenReturn(List.of(stepMissing));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(disabledPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(disabledPrestation, never()).disable(any());
        verify(stepMissing, never()).disable(any());
    }

    /**
     * CASO: Step vigente persistido, presente en stepAdvancements con enabled:true, y su PrestationInstance
     * padre viene en prestationExisting con enabled:false.
     * Validación: Lanza ConflictException, 409 — contradicción entre la baja del padre y la vigencia
     * declarada del hijo (regla #6/#9, sin excepción).
     */
    @Test
    void execute_stepEnabledTrueWhenParentPrestationDisabled_return409() {
        PrestationInstance disabledPrestation = mock(PrestationInstance.class);
        when(disabledPrestation.getId()).thenReturn(200L);

        PrestationStepInstance stepStillEnabled = mock(PrestationStepInstance.class);
        when(stepStillEnabled.getId()).thenReturn(300L);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);
        PrestationStepUpdateRequestDTO keepStep = new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.IN_PROGRESS, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(disabledPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(disabledPrestation)).thenReturn(List.of(stepStillEnabled));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(disabledPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(keepStep), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(disabledPrestation, never()).disable(any());
        verify(stepStillEnabled, never()).disable(any());
    }

    // ─── § 20/22/23: Validaciones de altas nuevas (sin cambios respecto a Create) ──

    /**
     * CASO: Prestación nueva con prestationStepId que no pertenece al prestationTypeId.
     * Validación: Lanza BadRequestException, 400.
     */
    @Test
    void execute_prestationStepNotInPrestation_return400() {
        PrestationInstanceRequestDTO prestacionDTO = new PrestationInstanceRequestDTO(
                10L, 200L, null, null, null, null, null, null, null, null, null, null);

        arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());

        doThrow(new BadRequestException("exception.validatePrestationStep.user", null,
                "exception.validatePrestationStep.log", new Object[]{200L, 10L}, null))
                .when(prestationInstanceDomainService).validatePrestationStep(10L, 200L);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(), List.of(), List.of(prestacionDTO))))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Prestación nueva isUnique con otra ya presente en prestationNew o en stepAdvancements no vacío.
     * Validación: Lanza ConflictException, 409.
     */
    @Test
    void execute_prestationUnique_return409() {
        PrestationInstanceRequestDTO p1 = new PrestationInstanceRequestDTO(
                10L, null, null, null, null, null, null, null, null, null, null, null);
        PrestationInstanceRequestDTO p2 = new PrestationInstanceRequestDTO(
                10L, null, null, null, null, null, null, null, null, null, null, null);

        arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());

        PrestationType prestationType = mock(PrestationType.class);
        when(prestationTypeService.findById(10L)).thenReturn(prestationType);

        doThrow(new ConflictException("exception.createConsultationInstanceUseCase.prestationUnique.user", null,
                "exception.createConsultationInstanceUseCase.prestationUnique.log", new Object[]{10L}, null))
                .when(prestationInstanceDomainService).validatePrestationUnique(eq(prestationType), eq(2), any(Boolean.class));

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(), List.of(), List.of(p1, p2))))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Prestación nueva con odontogram que no matchea ningún Odontogram guardado en este request.
     * Validación: Lanza ConflictException, 409 (ADR-0026, distinto de Create que usa 400).
     */
    @Test
    void execute_odontogramLocationMismatch_return409() {
        OdontogramRequestDTO odontogramDTO = new OdontogramRequestDTO(Tooth.T21, ToothFace.BOTTOM, 1L, 1L);
        PrestationInstanceRequestDTO prestacionDTO = new PrestationInstanceRequestDTO(
                10L, null, null, odontogramDTO, null, null, null, null, null, null, null, null);

        arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());

        PrestationType prestationType = mock(PrestationType.class);
        when(prestationTypeService.findById(10L)).thenReturn(prestationType);
        when(prestationInstanceDomainService.matchOdontogram(any(), any())).thenReturn(null);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(), List.of(), List.of(prestacionDTO))))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Avance de step sobre una prestación cuyo paciente no coincide con el de la consulta.
     * Validación: Lanza ConflictException, 409.
     */
    @Test
    void execute_stepAdvancementPatientMismatch_return409() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(null);

        PrestationStepUpdateRequestDTO advancement = new PrestationStepUpdateRequestDTO(200L, null, 1L, PrestationStepStatus.COMPLETED, true);
        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        doThrow(new ConflictException("exception.prestationInstanceDomainService.validateStepAdvancement.notOwner.user", null,
                "exception.prestationInstanceDomainService.validateStepAdvancement.notOwner.log", new Object[]{200L}, null))
                .when(prestationInstanceDomainService).validateStepAdvancement(eq(1L), eq(existingPrestation), any());

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID,
                dto(null, List.of(), List.of(keepPrestation), List.of(advancement), List.of())))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: dto.observation() distinto al persistido, sin cambios en odontograma/prestaciones.
     * Validación: consultationInstance.setObservation invocado con el nuevo valor; no se invoca ningún
     * disable()/save de odontograma o prestaciones.
     */
    @Test
    void execute_observationEdited_persisted() {
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto("Nueva observación", List.of(), List.of(), List.of(), List.of()));

        verify(instance).setObservation("Nueva observación");
        verify(odontogramRepository, never()).saveAll(any());
        verify(prestationInstanceRepository, never()).saveAll(any());
    }

    /**
     * CASO: Prestación nueva en la misma ubicación (odontograma) que una PrestationInstance dada de baja en
     * el mismo request.
     * Validación: validatePrestationInstancePending no rechaza — ve el estado post-delta, no el pre-delta.
     */
    @Test
    void execute_newPrestationInSameLocationAsJustDisabled_accepted() {
        Odontogram sharedOdontogram = mock(Odontogram.class);
        when(sharedOdontogram.getId()).thenReturn(50L);
        when(sharedOdontogram.getTooth()).thenReturn(Tooth.T11);
        when(sharedOdontogram.getToothFace()).thenReturn(ToothFace.TOP);
        Treatment persistedTreatment = mock(Treatment.class);
        when(persistedTreatment.getId()).thenReturn(1L);
        when(sharedOdontogram.getTreatment()).thenReturn(persistedTreatment);
        TreatmentCondition persistedTreatmentCondition = mock(TreatmentCondition.class);
        when(persistedTreatmentCondition.getId()).thenReturn(1L);
        when(sharedOdontogram.getTreatmentCondition()).thenReturn(persistedTreatmentCondition);

        PrestationInstance disabledPrestation = mock(PrestationInstance.class);
        when(disabledPrestation.getId()).thenReturn(200L);
        when(disabledPrestation.getOdontogram()).thenReturn(sharedOdontogram);

        OdontogramUpdateRequestDTO keepOdontogram = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        OdontogramRequestDTO odontogramDTO = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);
        PrestationInstanceRequestDTO newPrestation = new PrestationInstanceRequestDTO(
                10L, null, null, odontogramDTO, null, null, null, null, null, null, null, null);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(sharedOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(disabledPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(disabledPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(disabledPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        PrestationType newPrestationType = mock(PrestationType.class);
        when(newPrestationType.isRequiresLocation()).thenReturn(false);
        when(prestationTypeService.findById(10L)).thenReturn(newPrestationType);
        when(prestationInstanceDomainService.matchOdontogram(any(), any())).thenReturn(sharedOdontogram);
        PrestationTypePrice price = mock(PrestationTypePrice.class);
        when(price.getPrice()).thenReturn(BigDecimal.TEN);
        when(prestationTypePriceService.currentPrice(10L)).thenReturn(price);
        when(prestationInstanceRepository.saveAll(any())).thenReturn(List.of(mock(PrestationInstance.class)));
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(keepOdontogram), List.of(disablePrestation), List.of(), List.of(newPrestation)));

        verify(prestationInstanceDomainService).validatePrestationInstancePending(1L, newPrestation);
    }

    /**
     * CASO: Corrección exitosa sin errores.
     * Validación: consultationEventService.create invocado con eventType = CONSULTATION_INSTANCE_CORRECTED.
     */
    @Test
    void execute_eventCreated_consultationInstanceCorrected() {
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto("Corrección de prueba", List.of(), List.of(), List.of(), List.of()));

        ArgumentCaptor<ConsultationEvent> captor = ArgumentCaptor.forClass(ConsultationEvent.class);
        verify(consultationEventService, times(1)).create(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(ConsultationEventType.CONSULTATION_INSTANCE_CORRECTED);
        assertThat(captor.getValue().getConsultation()).isEqualTo(instance.getConsultation());
    }

    /**
     * CASO: Corrección válida que mantiene una PrestationInstance existente vigente (enabled:true, sin cambios).
     * Validación: Response.success = true con el id de la ConsultationInstance corregida; la prestación
     * persistida no se deshabilita.
     */
    @Test
    void execute_updateConsultaInstance_return200() {
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        PrestationInstance persistedPrestation = mock(PrestationInstance.class);
        when(persistedPrestation.getId()).thenReturn(200L);
        when(persistedPrestation.getOdontogram()).thenReturn(null);
        PrestationType persistedPrestationType = mock(PrestationType.class);
        when(persistedPrestationType.getId()).thenReturn(10L);
        when(persistedPrestation.getType()).thenReturn(persistedPrestationType);

        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID))
                .thenReturn(List.of(persistedPrestation));
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of());
        when(prestationStepInstanceRepository.findByPrestationInstance(persistedPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(persistedPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        Response<ConsultationInstanceBasicResponseDTO> result = useCase.execute(INSTANCE_ID,
                dto("Corrección completa", List.of(), List.of(keepPrestation), List.of(), List.of()));

        assertThat(result.success()).isTrue();
        assertThat(result.data().id()).isEqualTo(INSTANCE_ID);
        verify(persistedPrestation, never()).disable(any());
    }

    /**
     * CASO: Misma request: baja de una PrestationInstance con un step fuera de su propia ventana (409) +
     * baja de un step individual de OTRA prestación en la misma request.
     * Validación: Lanza ConflictException, 409; verificar que ningún save/disable de ningún sub-recurso de
     * la request se ejecutó (todo o nada).
     */
    @Test
    void execute_prestationOutOfWindowStepAndStepDisableSameRequest_return409() {
        PrestationInstance prestationWithStepOutOfWindow = mock(PrestationInstance.class);
        when(prestationWithStepOutOfWindow.getId()).thenReturn(200L);
        PrestationStepInstance stepOutOfWindow = mock(PrestationStepInstance.class);
        when(stepOutOfWindow.getId()).thenReturn(300L);

        PrestationInstance otherPrestation = mock(PrestationInstance.class);
        when(otherPrestation.getId()).thenReturn(201L);
        PrestationStepInstance otherStepToDisable = mock(PrestationStepInstance.class);

        PrestationInstanceUpdateRequestDTO disableFirstPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);
        PrestationStepUpdateRequestDTO disableStepOutOfWindow = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
        PrestationStepUpdateRequestDTO disableOtherStep = new PrestationStepUpdateRequestDTO(201L, 300L, null, null, false);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID))
                .thenReturn(List.of(prestationWithStepOutOfWindow, otherPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(prestationWithStepOutOfWindow))
                .thenReturn(List.of(stepOutOfWindow));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(any(), anyLong())).thenReturn(true);
        when(prestationInstanceDomainService.isStepEditable(stepOutOfWindow, CORRECTIONS_HOURS_LONG)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID,
                dto(null, List.of(), List.of(disableFirstPrestation), List.of(disableStepOutOfWindow, disableOtherStep), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(prestationWithStepOutOfWindow, never()).disable(any());
        verify(otherPrestation, never()).disable(any());
        verify(otherStepToDisable, never()).disable(any());
        verify(stepOutOfWindow, never()).disable(any());
    }

    // ─── § 3 regla #10: Reseteo de treatmentCondition ─────────────────────────

    /**
     * CASO: Baja explícita (enabled:false en prestationExisting) de una PrestationInstance con Odontogram
     * asociado (sigue enabled:true, no viene en el array de bajas de odontograma).
     * Validación: TreatmentConditionService.findByName(REQUIRED.name()) invocado; odontogram.setTreatmentCondition
     * seteado con la fila REQUIRED antes de persistir; el Odontogram sigue enabled=true.
     */
    @Test
    void execute_prestationInstanceDisabledExplicitWithOdontogram_resetsTreatmentConditionToRequired() {
        Odontogram linkedOdontogram = mock(Odontogram.class);
        when(linkedOdontogram.getId()).thenReturn(50L);
        when(linkedOdontogram.getTooth()).thenReturn(Tooth.T11);
        when(linkedOdontogram.getToothFace()).thenReturn(ToothFace.TOP);
        Treatment persistedTreatment = mock(Treatment.class);
        when(persistedTreatment.getId()).thenReturn(1L);
        when(linkedOdontogram.getTreatment()).thenReturn(persistedTreatment);
        TreatmentCondition persistedTreatmentCondition = mock(TreatmentCondition.class);
        when(persistedTreatmentCondition.getId()).thenReturn(1L);
        when(linkedOdontogram.getTreatmentCondition()).thenReturn(persistedTreatmentCondition);

        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(linkedOdontogram);

        OdontogramUpdateRequestDTO keepOdontogram = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        TreatmentCondition requiredCondition = mock(TreatmentCondition.class);
        when(treatmentConditionService.findByName(TreatmentConditionType.REQUIRED.name())).thenReturn(requiredCondition);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(keepOdontogram), List.of(disablePrestation), List.of(), List.of()));

        verify(treatmentConditionService).findByName(TreatmentConditionType.REQUIRED.name());
        verify(linkedOdontogram).setTreatmentCondition(requiredCondition);
        verify(linkedOdontogram, never()).disable(any());
    }

    /**
     * CASO: Odontogram dado de baja (enabled:false) con PrestationInstance viva vinculada, cancelada por
     * cascada (§ 3.4, regla #8).
     * Validación: TreatmentConditionService.findByName NUNCA invocado; no se setea ningún treatmentCondition.
     */
    @Test
    void execute_cascadeDisabledPrestation_doesNotResetTreatmentCondition() {
        OdontogramUpdateRequestDTO disableOdontogram = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getId()).thenReturn(50L);

        PrestationInstance linkedPrestation = mock(PrestationInstance.class);
        when(linkedPrestation.getId()).thenReturn(200L);
        when(linkedPrestation.getOdontogram()).thenReturn(existingOdontogram);

        PrestationInstanceUpdateRequestDTO declaredDisabled =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(linkedPrestation)).thenReturn(List.of());
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(disableOdontogram), List.of(declaredDisabled), List.of(), List.of()));

        verify(linkedPrestation).disable(any());
        verify(treatmentConditionService, never()).findByName(any());
        verify(existingOdontogram, never()).setTreatmentCondition(any());
    }

    /**
     * CASO: Baja explícita de una PrestationInstance sin Odontogram asociado (ubicación por scope, ej.
     * TOOTH/QUADRANT).
     * Validación: No lanza ninguna excepción; TreatmentConditionService.findByName no se invoca.
     */
    @Test
    void execute_prestationInstanceDisabledExplicitWithoutOdontogram_noResetAttempted() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(null);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(), List.of()));

        verify(existingPrestation).disable(any());
        verify(treatmentConditionService, never()).findByName(any());
    }

    // ─── § 3 regla #13/#16: Ventana propia de PrestationInstance / edición de ubicación in-place ──

    /**
     * CASO: PrestationInstance existente cuyo propio createdAt superó la ventana de corrección, referenciada
     * en prestationExisting con un cambio de ubicación.
     * Validación: Lanza ConflictException, 409 (regla #13) — no se aplica el cambio.
     */
    @Test
    void execute_prestationInstanceOutOfOwnWindowEdited_return409() {
        PrestationInstance outOfWindowPrestation = mock(PrestationInstance.class);
        when(outOfWindowPrestation.getId()).thenReturn(200L);

        PrestationInstanceUpdateRequestDTO editLocation =
                new PrestationInstanceUpdateRequestDTO(200L, PrestationScopeType.TOOTH, Tooth.T21, null, null, null, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(outOfWindowPrestation));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(outOfWindowPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(editLocation), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(outOfWindowPrestation, never()).disable(any());
        verify(outOfWindowPrestation, never()).setScope(any());
    }

    /**
     * CASO: Misma precondición que el test anterior, pero con enabled:false en vez de cambio de ubicación.
     * Validación: Lanza ConflictException, 409 — la ventana del contenedor bloquea también la baja explícita.
     */
    @Test
    void execute_prestationInstanceOutOfOwnWindowDisabled_return409() {
        PrestationInstance outOfWindowPrestation = mock(PrestationInstance.class);
        when(outOfWindowPrestation.getId()).thenReturn(200L);

        PrestationInstanceUpdateRequestDTO disablePrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, false);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(outOfWindowPrestation));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(outOfWindowPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(disablePrestation), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(outOfWindowPrestation, never()).disable(any());
    }

    /**
     * CASO: stepAdvancements con un elemento id==null, prestationStepId presente, prestationInstanceId de una
     * PrestationInstance ya persistida, enabled:true, status válido según orden del catálogo.
     * Validación: Se mapea a PrestationStepAdvancementRequestDTO y se invoca buildStepAdvancements;
     * se construye y persiste un PrestationStepInstance nuevo asociado a esa prestación existente, con las
     * mismas validaciones que Create.
     */
    @Test
    void execute_newStepOnExistingPrestation_buildsAndPersists() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(null);
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.getId()).thenReturn(10L);
        when(existingPrestation.getType()).thenReturn(prestationType);

        PrestationStep catalogStep = mock(PrestationStep.class);

        PrestationStepUpdateRequestDTO newStepDto =
                new PrestationStepUpdateRequestDTO(200L, null, 1L, PrestationStepStatus.IN_PROGRESS, true);
        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(catalogStep));

        PrestationStepInstance builtStep = mock(PrestationStepInstance.class);
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any()))
                .thenReturn(List.of(builtStep));
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(newStepDto), List.of()));

        ArgumentCaptor<List> stepAdvancementsCaptor = ArgumentCaptor.forClass(List.class);
        verify(prestationInstanceDomainService).buildStepAdvancements(any(), any(), stepAdvancementsCaptor.capture(), any(), any());
        assertThat(stepAdvancementsCaptor.getValue()).hasSize(1);
        verify(prestationStepInstanceRepository).saveAll(List.of(builtStep));
    }

    /**
     * CASO: stepAdvancements con un elemento id existente (step persistido IN_PROGRESS, id=300), enabled:true,
     * status:COMPLETED; se usa un PrestationInstanceDomainService REAL (no mockeado) inyectado en el use case
     * para ejercitar applyStepAdvancement de punta a punta, sin stub sobre ese método.
     * Regla: el avance de un step existente debe editar el registro persistido in-place, nunca pasar por
     * buildStepAdvancements (que SIEMPRE construye un registro nuevo vía PrestationStepInstance.build).
     * Validación: El PrestationStepInstance persistido (id=300) queda con status=COMPLETED sobre el MISMO id;
     * prestationStepInstanceRepository.saveAll NUNCA recibe un PrestationStepInstance nuevo con id==null/distinto
     * para este step.
     */
    @Test
    void execute_existingStepAdvancement_updatesInPlaceWithoutDuplicating() throws Exception {
        // Constructor package-private (mismo criterio que @InjectMocks usa vía reflexión en PrestationInstanceDomainServiceTest).
        // Se reusan los mismos @Mock de clase (prestationStepService, prestationInstanceRepository) que el resto de la
        // suite, ya que el use case bajo test también los referencia por esos campos.
        java.lang.reflect.Constructor<PrestationInstanceDomainService> realDomainServiceConstructor =
                PrestationInstanceDomainService.class.getDeclaredConstructor(
                        PrestationStepService.class, IPrestationInstanceRepository.class, PrestationStepInstanceQueryService.class);
        realDomainServiceConstructor.setAccessible(true);
        PrestationStepInstanceQueryService realPrestationStepInstanceQueryService = mock(PrestationStepInstanceQueryService.class);
        PrestationInstanceDomainService realDomainService = realDomainServiceConstructor.newInstance(
                prestationStepService, prestationInstanceRepository, realPrestationStepInstanceQueryService);

        UpdateConsultationInstanceUseCase useCaseWithRealDomainService = new UpdateConsultationInstanceUseCase(
                consultationInstanceQueryService, authenticatedUserService, systemParameterService,
                consultationInstanceRepository, odontogramRepository, prestationInstanceRepository,
                realDomainService, prestationTypeService, prestationTypePriceService, prestationStepService,
                prestationStepInstanceRepository, treatmentService, treatmentConditionService, promotionQueryService,
                messageSource, consultationEventService, consultationMapper);

        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.getId()).thenReturn(10L);

        ConsultationInstance originConsultationInstance = mock(ConsultationInstance.class);
        when(originConsultationInstance.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(1));

        Consultation existingPrestationConsultation = mock(Consultation.class);
        Patient existingPrestationPatient = mock(Patient.class);
        when(existingPrestationPatient.getId()).thenReturn(1L);
        when(existingPrestationConsultation.getPatient()).thenReturn(existingPrestationPatient);
        ConsultationInstance existingPrestationConsultationInstance = mock(ConsultationInstance.class);
        when(existingPrestationConsultationInstance.getConsultation()).thenReturn(existingPrestationConsultation);

        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(null);
        when(existingPrestation.getType()).thenReturn(prestationType);
        when(existingPrestation.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(existingPrestation.getConsultationInstance()).thenReturn(existingPrestationConsultationInstance);
        when(existingPrestation.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(1));

        PrestationStep catalogStep = mock(PrestationStep.class);
        when(catalogStep.getId()).thenReturn(1L);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(catalogStep));

        PrestationStepInstance existingStepInstance = PrestationStepInstance.build(
                existingPrestation, originConsultationInstance, catalogStep, PrestationStepStatus.IN_PROGRESS);
        existingStepInstance.setId(300L);

        PrestationStepUpdateRequestDTO advanceExistingStep =
                new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.COMPLETED, true);
        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of(existingStepInstance));
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCaseWithRealDomainService.execute(INSTANCE_ID,
                dto(null, List.of(), List.of(keepPrestation), List.of(advanceExistingStep), List.of()));

        assertThat(existingStepInstance.getId()).isEqualTo(300L);
        assertThat(existingStepInstance.getStatus()).isEqualTo(PrestationStepStatus.COMPLETED);

        ArgumentCaptor<List<PrestationStepInstance>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(prestationStepInstanceRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue())
                .noneMatch(saved -> saved.getStep().getId().equals(1L) && !saved.getId().equals(300L));
    }

    /**
     * CASO: stepAdvancements con id==null y enabled:false en el mismo elemento.
     * Validación: Lanza excepción de validación, 400 en el binding (@AssertTrue) — payload contradictorio.
     * En este UseCaseTest se verifica a nivel de dominio: el DTO en sí ya no es constructible sin contradicción
     * (isNotContradictory se testea en su propia suite); acá se documenta que el UseCase nunca llega a
     * procesar un elemento así porque Bean Validation lo rechaza antes del binding.
     */
    @Test
    void execute_newStepOnExistingPrestationWithDisabledTrue_return400() {
        PrestationStepUpdateRequestDTO contradictory = new PrestationStepUpdateRequestDTO(200L, null, 1L, null, false);

        assertThat(contradictory.isNotContradictory()).isFalse();
    }

    /**
     * CASO: stepAdvancements con id==null y sin prestationStepId en el mismo elemento.
     * Validación: Lanza excepción de validación, 400 en el binding (@AssertTrue) — payload sin intención
     * expresable, el backend no sabe qué PrestationStep construir.
     */
    @Test
    void execute_newStepOnExistingPrestationWithoutPrestationStepId_return400() {
        PrestationStepUpdateRequestDTO missingStepId = new PrestationStepUpdateRequestDTO(200L, null, null, PrestationStepStatus.IN_PROGRESS, true);

        assertThat(missingStepId.isPrestationStepIdDeclaredWhenNew()).isFalse();
    }

    /**
     * CASO: Elemento de stepAdvancements con prestationInstanceId que no corresponde a ninguna
     * PrestationInstance persistida de esta ConsultationInstance.
     * Validación: Lanza NotFoundException, 404, antes de procesar el resto del delta de ese elemento.
     */
    @Test
    void execute_stepAdvancementPrestationInstanceIdNotFound_return404() {
        PrestationStepUpdateRequestDTO advancementOnUnknownPrestation =
                new PrestationStepUpdateRequestDTO(999L, null, 1L, PrestationStepStatus.IN_PROGRESS, true);

        arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID,
                dto(null, List.of(), List.of(), List.of(advancementOnUnknownPrestation), List.of())))
                .isInstanceOf(NotFoundException.class);

        verify(prestationInstanceDomainService, never()).buildStepAdvancements(any(), any(), any(), any(), any());
    }

    /**
     * CASO: Elemento de stepAdvancements con prestationInstanceId que coincide con un identificador arbitrario
     * que el front asoció a una prestación de prestationNew (sin id real de BD todavía).
     * Validación: Lanza NotFoundException, 404 — stepAdvancements nunca referencia prestaciones de
     * prestationNew, solo prestaciones ya persistidas.
     */
    @Test
    void execute_stepAdvancementPrestationInstanceIdFromPrestationNew_return404() {
        PrestationInstanceRequestDTO newPrestation = new PrestationInstanceRequestDTO(
                10L, null, null, null, PrestationScopeType.FULL_MOUTH, null, null, null, null, null, null, null);
        // El front usa un id "provisorio" (777L) que en realidad nunca fue persistido — no hay tal prestation existente.
        PrestationStepUpdateRequestDTO advancementOnNewPrestation =
                new PrestationStepUpdateRequestDTO(777L, null, 1L, PrestationStepStatus.IN_PROGRESS, true);

        arrangeBaseWithinWindow(1L);
        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID,
                dto(null, List.of(), List.of(), List.of(advancementOnNewPrestation), List.of(newPrestation))))
                .isInstanceOf(NotFoundException.class);
    }

    /**
     * CASO: PrestationInstance existente sin Odontogram asociado (ubicación por scope), referenciada en
     * prestationExisting con tooth distinto al persistido, ubicación nueva compatible con allowedScopes de su
     * PrestationType.
     * Validación: El registro persistido se actualiza in-place (mismo id) con la ubicación corregida; no se
     * invoca disable(), no se persiste ningún registro nuevo.
     */
    @Test
    void execute_prestationInstanceLocationEdited_updatesInPlace() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(null);
        when(existingPrestation.getScope()).thenReturn(PrestationScopeType.TOOTH);
        when(existingPrestation.getTooth()).thenReturn(Tooth.T11);
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.getId()).thenReturn(10L);
        when(existingPrestation.getType()).thenReturn(prestationType);

        PrestationInstanceUpdateRequestDTO editLocation =
                new PrestationInstanceUpdateRequestDTO(200L, PrestationScopeType.TOOTH, Tooth.T21, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of());
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(editLocation), List.of(), List.of()));

        verify(existingPrestation, never()).disable(any());
        verify(prestationInstanceRepository, never()).saveAll(any());
        verify(existingPrestation).setTooth(Tooth.T21);
        verify(existingPrestation).setScope(PrestationScopeType.TOOTH);
    }

    /**
     * CASO: PrestationInstance sin Odontogram con un pago parcial ya registrado, editada por ubicación en
     * el mismo request.
     * Validación: El registro se actualiza in-place, mismo id; el pago parcial no bloquea la edición de
     * ubicación ni dispara disable().
     */
    @Test
    void execute_prestationInstanceLocationEditedWithPartialPayment_updatesInPlaceWithoutDisabling() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(null);
        when(existingPrestation.getScope()).thenReturn(PrestationScopeType.TOOTH);
        when(existingPrestation.getTooth()).thenReturn(Tooth.T11);
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.getId()).thenReturn(10L);
        when(existingPrestation.getType()).thenReturn(prestationType);

        PrestationInstanceUpdateRequestDTO editLocation =
                new PrestationInstanceUpdateRequestDTO(200L, PrestationScopeType.TOOTH, Tooth.T21, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of());
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(editLocation), List.of(), List.of()));

        verify(existingPrestation, never()).disable(any());
        verify(existingPrestation).setTooth(Tooth.T21);
    }

    /**
     * CASO: PrestationInstance existente sin Odontogram, referenciada en prestationExisting con un scope
     * fuera de PrestationType.allowedScopes.
     * Validación: Lanza ConflictException, 409 (validateScopeAllowedForType) — el cambio no se aplica.
     */
    @Test
    void execute_prestationInstanceLocationEditedScopeNotAllowed_return409() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        PrestationType prestationType = mock(PrestationType.class);
        when(existingPrestation.getType()).thenReturn(prestationType);

        PrestationInstanceUpdateRequestDTO editLocation =
                new PrestationInstanceUpdateRequestDTO(200L, PrestationScopeType.QUADRANT, null, null, com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Quadrant.UPPER_RIGHT, null, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        doThrow(new ConflictException("exception.prestationInstanceDomainService.validateScopeAllowedForType.user", null,
                "exception.prestationInstanceDomainService.validateScopeAllowedForType.log", new Object[]{PrestationScopeType.QUADRANT}, null))
                .when(prestationInstanceDomainService).validateScopeAllowedForType(prestationType, PrestationScopeType.QUADRANT);

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(editLocation), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(existingPrestation, never()).disable(any());
        verify(existingPrestation, never()).setScope(any());
        verify(prestationInstanceRepository, never()).saveAll(any());
    }

    /**
     * CASO: PrestationInstance existente CON Odontogram asociado (persistido), referenciada en
     * prestationExisting con scope/tooth no nulos.
     * Validación: Lanza ConflictException, 409 — no 400 (regla #17/#18, validateLocationNotOverridable).
     */
    @Test
    void execute_prestationInstanceWithOdontogramReceivesLocationFields_return409() {
        Odontogram linkedOdontogram = mock(Odontogram.class);
        when(linkedOdontogram.getId()).thenReturn(50L);
        when(linkedOdontogram.getTooth()).thenReturn(Tooth.T11);
        when(linkedOdontogram.getToothFace()).thenReturn(ToothFace.TOP);
        Treatment persistedTreatment = mock(Treatment.class);
        when(persistedTreatment.getId()).thenReturn(1L);
        when(linkedOdontogram.getTreatment()).thenReturn(persistedTreatment);
        TreatmentCondition persistedTreatmentCondition = mock(TreatmentCondition.class);
        when(persistedTreatmentCondition.getId()).thenReturn(1L);
        when(linkedOdontogram.getTreatmentCondition()).thenReturn(persistedTreatmentCondition);

        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);

        OdontogramUpdateRequestDTO keepOdontogram = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
        PrestationInstanceUpdateRequestDTO invalidLocationOverride =
                new PrestationInstanceUpdateRequestDTO(200L, PrestationScopeType.TOOTH, Tooth.T21, null, null, null, true);

        arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(linkedOdontogram));
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);

        doThrow(new ConflictException("exception.prestationInstanceDomainService.validateLocationNotOverridable.user", null,
                "exception.prestationInstanceDomainService.validateLocationNotOverridable.log", new Object[]{200L}, null))
                .when(prestationInstanceDomainService).validateLocationNotOverridable(eq(existingPrestation), eq(invalidLocationOverride));

        assertThatThrownBy(() -> useCase.execute(INSTANCE_ID, dto(null, List.of(keepOdontogram), List.of(invalidLocationOverride), List.of(), List.of())))
                .isInstanceOf(ConflictException.class);

        verify(existingPrestation, never()).disable(any());
        verify(existingPrestation, never()).setScope(any());
        verify(prestationInstanceRepository, never()).saveAll(any());
    }

    /**
     * CASO: PrestationInstance existente referenciada en prestationExisting; el DTO no expone
     * price/promotionAmount/discountAmount/finalAmount/prestationTypeId como campos editables.
     * Validación: El registro persistido conserva su price/type/derivados originales tras la corrección — no
     * hay mecanismo para sobrescribirlos desde este endpoint.
     */
    @Test
    void execute_prestationInstancePriceAndTypeImmutable_notOverwritten() {
        PrestationInstance existingPrestation = mock(PrestationInstance.class);
        when(existingPrestation.getId()).thenReturn(200L);
        when(existingPrestation.getOdontogram()).thenReturn(null);
        BigDecimal originalPrice = new BigDecimal("500");
        when(existingPrestation.getPrice()).thenReturn(originalPrice);
        PrestationType existingPrestationType = mock(PrestationType.class);
        when(existingPrestationType.getId()).thenReturn(10L);
        when(existingPrestation.getType()).thenReturn(existingPrestationType);

        PrestationInstanceUpdateRequestDTO keepPrestation =
                new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, true);

        ConsultationInstance instance = arrangeBaseWithinWindow(1L);

        when(odontogramRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(INSTANCE_ID)).thenReturn(List.of(existingPrestation));
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of());
        when(prestationStepInstanceRepository.findByPrestationInstance(existingPrestation)).thenReturn(List.of());
        when(prestationInstanceDomainService.isPrestationInstanceEditable(existingPrestation, CORRECTIONS_HOURS_LONG)).thenReturn(true);
        when(prestationInstanceDomainService.buildStepAdvancements(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(consultationEventService.create(any())).thenReturn(mock(ConsultationEvent.class));
        arrangeHappyResponseStubs(instance.getConsultation());

        useCase.execute(INSTANCE_ID, dto(null, List.of(), List.of(keepPrestation), List.of(), List.of()));

        verify(existingPrestation, never()).setPrice(any());
        verify(existingPrestation, never()).setType(any());
        verify(prestationInstanceRepository, never()).saveAll(any());
        assertThat(existingPrestation.getPrice()).isEqualTo(originalPrice);
    }
}
