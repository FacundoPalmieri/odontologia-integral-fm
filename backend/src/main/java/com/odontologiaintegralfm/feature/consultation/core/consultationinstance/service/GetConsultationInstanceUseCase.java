package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.repository.IOdontogramRepository;
import com.odontologiaintegralfm.feature.consultation.core.payment.repository.IPaymentDetailRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.mapper.PrestationInstanceMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GetConsultationInstanceUseCase {

    private final ConsultationInstanceQueryService consultationInstanceQueryService;
    private final IPrestationInstanceRepository prestationInstanceRepository;
    private final IPaymentDetailRepository paymentDetailRepository;
    private final IOdontogramRepository odontogramRepository;
    private final PrestationInstanceMapper prestationInstanceMapper;
    private final OdontogramMapper odontogramMapper;
    private final ConsultationMapper consultationMapper;
    private final AuthenticatedUserService authenticatedUserService;

    public GetConsultationInstanceUseCase(ConsultationInstanceQueryService consultationInstanceQueryService,
                                          IPrestationInstanceRepository prestationInstanceRepository,
                                          IPaymentDetailRepository paymentDetailRepository,
                                          IOdontogramRepository odontogramRepository,
                                          PrestationInstanceMapper prestationInstanceMapper,
                                          OdontogramMapper odontogramMapper,
                                          ConsultationMapper consultationMapper,
                                          AuthenticatedUserService authenticatedUserService) {
        this.consultationInstanceQueryService = consultationInstanceQueryService;
        this.prestationInstanceRepository = prestationInstanceRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.odontogramRepository = odontogramRepository;
        this.prestationInstanceMapper = prestationInstanceMapper;
        this.odontogramMapper = odontogramMapper;
        this.consultationMapper = consultationMapper;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional(readOnly = true)
    public Response<ConsultationInstanceResponseDTO> execute(Long id) {

        ConsultationInstance instance = consultationInstanceQueryService.findById(id);

        UserSec user = authenticatedUserService.getAuthenticatedUser();
        boolean isDentist = user.getRolesList().stream()
                .anyMatch(r -> r.getName().equals(Role.DENTIST.name()));

        if (isDentist) {
            Long dentistPersonId = instance.getConsultation().getDentist().getPerson().getId();
            Long userPersonId = user.getPerson().getId();
            if (!dentistPersonId.equals(userPersonId)) {
                throw new ForbiddenException(
                        "exception.getConsultationInstanceUseCase.forbidden.user", null,
                        "exception.getConsultationInstanceUseCase.forbidden.log", new Object[]{id, userPersonId, "GetConsultationInstanceUseCase", "execute"},
                        LogLevel.WARN
                );
            }
        }

        List<PrestationInstance> prestations = prestationInstanceRepository.findByConsultationInstanceId(instance.getId());

        List<PrestationInstanceResponseDTO> prestationDTOs = prestations.stream()
                .map(p -> {
                    BigDecimal paid = paymentDetailRepository.sumAmountByPrestationInstanceId(p.getId());
                    BigDecimal pending = p.getFinalAmount().subtract(paid);
                    PrestationInstanceResponseDTO base = prestationInstanceMapper.toDTO(p);
                    return new PrestationInstanceResponseDTO(
                            base.id(), base.name(), base.odontogram(), base.scope(), base.scopeDetail(),
                            base.status(), base.price(), base.promotion(), base.promotionAmount(),
                            base.discountType(), base.discountValue(), base.discountAmount(),
                            base.finalAmount(), pending
                    );
                })
                .toList();

        BigDecimal totalFinalAmount = prestations.stream()
                .map(PrestationInstance::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Response<>(
                true,
                null,
                new ConsultationInstanceResponseDTO(
                        instance.getId(),
                        consultationMapper.toDTO(instance.getConsultation()),
                        odontogramRepository.findByConsultationInstanceId(instance.getId()).stream()
                                .map(odontogramMapper::toDTO).toList(),
                        prestationDTOs,
                        instance.getObservation(),
                        totalFinalAmount
                )
        );
    }
}
