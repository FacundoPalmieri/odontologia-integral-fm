package com.odontologiaintegralfm.feature.consultation.paymentaccount.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.dto.PaymentProviderDTOResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAccountDTOResponse {
    private Long id;
    private String alias;
    private String accountIdentifier;
    private String holderName;
    private PaymentProviderDTOResponse provider;
    private Boolean enabled;
}
