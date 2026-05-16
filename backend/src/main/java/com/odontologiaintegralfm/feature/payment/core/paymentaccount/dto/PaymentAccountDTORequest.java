package com.odontologiaintegralfm.feature.payment.core.paymentaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @author [Facundo Palmieri]
 */
public record PaymentAccountDTORequest(

        @NotBlank(message = "paymentAccountDTORequest.alias.empty")
        @Size(max = 100, message = "paymentAccountDTORequest.alias.size")
        String alias,

        @NotBlank(message = "paymentAccountDTORequest.accountIdentifier.empty")
        @Size(max = 22, message = "paymentAccountDTORequest.accountIdentifier.size")
        @Pattern(regexp = "\\d+", message = "paymentAccountDTORequest.accountIdentifier.numeric")
        String accountIdentifier,

        @NotBlank(message = "paymentAccountDTORequest.holderName.empty")
        @Size(max = 100, message = "paymentAccountDTORequest.holderName.size")
        String holderName,

        @NotNull(message = "paymentAccountDTORequest.providerId.empty")
        Long providerId

) {
}
