package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository.IPrestationStepRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.mapper.PrestationStepMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetNextStepsPrestationInstanceUseCaseTest {

    @Mock private PrestationInstanceQueryService prestationInstanceQueryService;
    @Mock private IPrestationStepRepository prestationStepRepository;
    @Mock private IPrestationStepInstanceRepository prestationStepInstanceRepository;
    @Mock private PrestationStepMapper prestationStepMapper;

    @InjectMocks private GetNextStepsPrestationInstanceUseCase useCase;

    /**
     * CASO: La PrestationInstance no existe o está deshabilitada.
     * Regla: El QueryService encapsula el NotFoundException — el use case no lo atrapa.
     * Validación: NotFoundException se propaga tal cual, sin wrapping.
     */
    @Test
    void getNextSteps_prestationInstanceDeshabilitada_retorna404() {
        when(prestationInstanceQueryService.findById(1L))
                .thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(NotFoundException.class);
    }

    /**
     * CASO: La PrestationInstance existe pero su estado no es IN_PROGRESS (ej: COMPLETED).
     * Regla: Solo se pueden consultar los próximos steps de una prestación en progreso.
     * Validación: BadRequestException con el label del estado actual en el mensaje.
     */
    @Test
    void getNextSteps_prestationInstanceNoInProgress_retorna422() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.COMPLETED);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: La PrestationInstance está IN_PROGRESS pero su tipo no tiene workflow de steps.
     * Regla: Verificar hasSteps antes de consultar el repositorio evita un 404 engañoso.
     * Validación: BadRequestException lanzada antes de cualquier llamada al repositorio de steps.
     */
    @Test
    void getNextSteps_prestationInstanceSinWorkflowDeSteps_retorna422() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(instance.getType().isHasSteps()).thenReturn(false);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: La PrestationInstance está IN_PROGRESS, hasSteps=true, pero el catálogo no tiene
     *       steps habilitados para ese tipo de prestación.
     * Regla: Lista vacía de steps del catálogo es un error de configuración → 404.
     * Validación: NotFoundException lanzada; el repositorio de step instances no es consultado.
     */
    @Test
    void getNextSteps_prestationStepsNoTieneSteps_retorna404() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(instance.getType().isHasSteps()).thenReturn(true);
        when(instance.getType().getId()).thenReturn(10L);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);
        when(prestationStepRepository.findByPrestationId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(NotFoundException.class);
    }

    /**
     * CASO: Catálogo con 1 step (required=true, position=1). Sin PrestationStepInstance registrados.
     * Regla: Sin avance previo, el punto de partida es antes del primero.
     * Validación: Resultado contiene exactamente ese step; tamaño de lista = 1.
     */
    @Test
    void getNextSteps_prestationInstanceStepsSinStep_retornaListPrimerStep200() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(instance.getType().isHasSteps()).thenReturn(true);
        when(instance.getType().getId()).thenReturn(10L);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);

        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getPosition()).thenReturn(1);
        when(step1.isRequired()).thenReturn(true);
        when(prestationStepRepository.findByPrestationId(10L)).thenReturn(List.of(step1));
        when(prestationStepInstanceRepository.findByPrestationInstance(instance)).thenReturn(List.of());

        PrestationStepResponseDTO dto1 = new PrestationStepResponseDTO(1L, "Paso 1", 1, true);
        when(prestationStepMapper.toDTO(List.of(step1))).thenReturn(List.of(dto1));

        List<PrestationStepResponseDTO> result = useCase.execute(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(dto1);
    }

    /**
     * CASO: Catálogo con 2 steps: step1 (required=false, pos=1), step2 (required=true, pos=2).
     *       Sin PrestationStepInstance registrados.
     * Regla: Si el primer step no es required, acumular hasta encontrar uno required.
     * Validación: Resultado contiene los 2 steps en orden; tamaño = 2.
     */
    @Test
    void getNextSteps_prestationInstanceStepsSinStep_retornaList200() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(instance.getType().isHasSteps()).thenReturn(true);
        when(instance.getType().getId()).thenReturn(10L);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);

        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getPosition()).thenReturn(1);
        when(step1.isRequired()).thenReturn(false);

        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getPosition()).thenReturn(2);
        when(step2.isRequired()).thenReturn(true);

        when(prestationStepRepository.findByPrestationId(10L)).thenReturn(List.of(step1, step2));
        when(prestationStepInstanceRepository.findByPrestationInstance(instance)).thenReturn(List.of());

        PrestationStepResponseDTO dto1 = new PrestationStepResponseDTO(1L, "Paso 1", 1, false);
        PrestationStepResponseDTO dto2 = new PrestationStepResponseDTO(2L, "Paso 2", 2, true);
        when(prestationStepMapper.toDTO(List.of(step1, step2))).thenReturn(List.of(dto1, dto2));

        List<PrestationStepResponseDTO> result = useCase.execute(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(dto1);
        assertThat(result.get(1)).isSameAs(dto2);
    }

    /**
     * CASO: Catálogo con 2 steps: step1 (required=true, pos=1), step2 (required=true, pos=2).
     *       Una PrestationStepInstance con step1 en COMPLETED.
     * Regla: El punto de partida es la posición máxima COMPLETED/OMITTED; step1 ya procesado.
     * Validación: Resultado contiene solo step2; step1 no aparece.
     */
    @Test
    void getNextSteps_prestationInstanceStepsConRegistrosAsociados_retornaSiguienteStep200() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(instance.getType().isHasSteps()).thenReturn(true);
        when(instance.getType().getId()).thenReturn(10L);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);

        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getPosition()).thenReturn(1);

        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getPosition()).thenReturn(2);
        when(step2.isRequired()).thenReturn(true);

        when(prestationStepRepository.findByPrestationId(10L)).thenReturn(List.of(step1, step2));

        PrestationStepInstance stepInstance1 = mock(PrestationStepInstance.class);
        when(stepInstance1.getStep()).thenReturn(step1);
        when(stepInstance1.getStatus()).thenReturn(PrestationStepStatus.COMPLETED);
        when(prestationStepInstanceRepository.findByPrestationInstance(instance)).thenReturn(List.of(stepInstance1));

        PrestationStepResponseDTO dto2 = new PrestationStepResponseDTO(2L, "Paso 2", 2, true);
        when(prestationStepMapper.toDTO(List.of(step2))).thenReturn(List.of(dto2));

        List<PrestationStepResponseDTO> result = useCase.execute(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(dto2);
    }

    /**
     * CASO: Catálogo con 3 steps: step1 (required=true, pos=1), step2 (required=false, pos=2),
     *       step3 (required=true, pos=3). Una PrestationStepInstance con step1 en COMPLETED.
     * Regla: Desde el punto de partida, acumular opcionales hasta llegar al siguiente required.
     * Validación: Resultado contiene step2 y step3; tamaño = 2.
     */
    @Test
    void getNextSteps_prestationInstanceStepsConStepNoRequerido_retornaListSiguientesStep200() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(instance.getType().isHasSteps()).thenReturn(true);
        when(instance.getType().getId()).thenReturn(10L);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);

        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getPosition()).thenReturn(1);

        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getPosition()).thenReturn(2);
        when(step2.isRequired()).thenReturn(false);

        PrestationStep step3 = mock(PrestationStep.class);
        when(step3.getPosition()).thenReturn(3);
        when(step3.isRequired()).thenReturn(true);

        when(prestationStepRepository.findByPrestationId(10L)).thenReturn(List.of(step1, step2, step3));

        PrestationStepInstance stepInstance1 = mock(PrestationStepInstance.class);
        when(stepInstance1.getStep()).thenReturn(step1);
        when(stepInstance1.getStatus()).thenReturn(PrestationStepStatus.COMPLETED);
        when(prestationStepInstanceRepository.findByPrestationInstance(instance)).thenReturn(List.of(stepInstance1));

        PrestationStepResponseDTO dto2 = new PrestationStepResponseDTO(2L, "Paso 2", 2, false);
        PrestationStepResponseDTO dto3 = new PrestationStepResponseDTO(3L, "Paso 3", 3, true);
        when(prestationStepMapper.toDTO(List.of(step2, step3))).thenReturn(List.of(dto2, dto3));

        List<PrestationStepResponseDTO> result = useCase.execute(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(dto2);
        assertThat(result.get(1)).isSameAs(dto3);
    }

    /**
     * CASO: Catálogo con 2 steps: step1 (required=true, pos=1), step2 (required=true, pos=2).
     *       Una PrestationStepInstance con step1 en OMITTED.
     * Regla: OMITTED equivale a COMPLETED para determinar el punto de partida.
     * Validación: Resultado contiene solo step2 — OMITTED cuenta como procesado.
     */
    @Test
    void getNextSteps_prestationInstanceStepsOMITTED_retornaListSiguientesStep200() {
        PrestationInstance instance = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(instance.getType().isHasSteps()).thenReturn(true);
        when(instance.getType().getId()).thenReturn(10L);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(instance);

        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getPosition()).thenReturn(1);

        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getPosition()).thenReturn(2);
        when(step2.isRequired()).thenReturn(true);

        when(prestationStepRepository.findByPrestationId(10L)).thenReturn(List.of(step1, step2));

        PrestationStepInstance stepInstance1 = mock(PrestationStepInstance.class);
        when(stepInstance1.getStep()).thenReturn(step1);
        when(stepInstance1.getStatus()).thenReturn(PrestationStepStatus.OMITTED);
        when(prestationStepInstanceRepository.findByPrestationInstance(instance)).thenReturn(List.of(stepInstance1));

        PrestationStepResponseDTO dto2 = new PrestationStepResponseDTO(2L, "Paso 2", 2, true);
        when(prestationStepMapper.toDTO(List.of(step2))).thenReturn(List.of(dto2));

        List<PrestationStepResponseDTO> result = useCase.execute(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(dto2);
    }
}