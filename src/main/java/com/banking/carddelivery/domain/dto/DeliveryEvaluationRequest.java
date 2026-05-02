package com.banking.carddelivery.domain.dto;

import com.banking.carddelivery.domain.enums.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvaluationRequest {

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido. Formato esperado: 99999-999 ou 99999999")
    private String cep;

    @NotNull(message = "Tipo do cartão é obrigatório")
    private CardType cardType;

    @NotBlank(message = "CustomerId é obrigatório")
    @Pattern(regexp = "^C\\d{6,12}$", message = "CustomerId inválido. Formato esperado: C seguido de 6 a 12 dígitos")
    private String customerId;
}
