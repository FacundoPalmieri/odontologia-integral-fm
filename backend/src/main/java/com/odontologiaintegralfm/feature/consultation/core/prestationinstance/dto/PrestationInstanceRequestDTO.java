package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PrestationInstanceRequestDTO(

        @NotNull(message = "prestationInstanceRequestDTO.prestationTypeId.empty")
        Long prestationTypeId,

        Long prestationStepId,

        PrestationStepStatus prestationStepStatus,

        //--------------------------------------------------------------------------------------------------------------------//
        /** Validar si envían odontogram que no envíen datos de ubicación*/

        OdontogramRequestDTO odontogram,

        //--------------------------------------------------------------------------------------------------------------------//

        /** Validar si NO envían odontogram que envíen datos de ubicación*/
        PrestationScopeType scope,

        Tooth tooth,

        ToothFace toothFace,

        Quadrant quadrant,

        Maxillary maxillary,

        //------------------------------------------------------------------------------------------------//
        /** Si hay promoción no puede haber descuento manual, y viceversa. El sistema debe validar esto.*/
        /** Promoción */
        Long promotionId,


        /** Descuento manual */
        /** Si el descuento viene en porcentaje, se calcula y se aplica*/
        DiscountType discountType,

        @Positive
        BigDecimal discountValue
) {
}
