package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

public record PrestationStepInstanceResponseDTO(

        // id del PrestationStepInstance persistido — el front lo referencia en stepAdvancements en una corrección posterior
        Long id,

        // id del PrestationStep de catálogo — necesario para altas de step nuevo sobre prestación existente
        Long prestationStepId,

        String nameStep,

        int position,

        boolean required,

        String status

) {
}