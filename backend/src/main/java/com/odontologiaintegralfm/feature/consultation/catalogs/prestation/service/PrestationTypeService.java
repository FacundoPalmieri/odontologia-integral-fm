package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.dto.PrestationTypeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.mapper.PrestationTypeMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository.IPrestationTypePriceRepository;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository.IPrestationTypeRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestationTypeService {

    private final IPrestationTypeRepository prestationTypeRepository;
    private final IPrestationTypePriceRepository prestationTypePriceRepository;
    private final PrestationTypeMapper prestationTypeMapper;

    PrestationTypeService(IPrestationTypeRepository prestationTypeRepository,
                          IPrestationTypePriceRepository prestationTypePriceRepository,
                          PrestationTypeMapper prestationTypeMapper) {
        this.prestationTypeRepository = prestationTypeRepository;
        this.prestationTypePriceRepository = prestationTypePriceRepository;
        this.prestationTypeMapper = prestationTypeMapper;
    }

    public PrestationType findById(Long id) {
        return prestationTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.prestationTypeService.notFound.user",null,"exception.prestationTypeService.notFound.log", new Object[]{id, "prestationTypeService","findById"}, LogLevel.ERROR));
    }

    @Transactional(readOnly = true)
    public Response<List<PrestationTypeResponseDTO>> getAll() {
        try {
            List<PrestationTypeResponseDTO> prestations = prestationTypeMapper.toDTO(
                    prestationTypePriceRepository.findAllActive(LocalDate.now())
            );
            return new Response<>(true, null, prestations);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PrestationTypeService", null, null, "getAll");
        }
    }
}
