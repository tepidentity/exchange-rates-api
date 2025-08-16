package com.zetta.exchangerates.dto;

import com.zetta.exchangerates.entity.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ConversionDTO {

    @NotNull private Currency sourceCurrency;
    @NotNull private Currency targetCurrency;
    @NotNull private Double amount;
}
