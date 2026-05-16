package com.odontologiaintegralfm.feature.consultation.core.odontogram.service;


import com.odontologiaintegralfm.feature.consultation.core.odontogram.repository.IOdontogramRepository;
import org.springframework.stereotype.Service;

@Service
public class OdontogramQueryService {

    private final IOdontogramRepository odontogramRepository;

    public OdontogramQueryService(IOdontogramRepository odontogramRepository) {
        this.odontogramRepository = odontogramRepository;
    }

}
