package com.zetta.exchangerates.dto;

import com.zetta.exchangerates.entity.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ConversionDTO(@NotNull Currency sourceCurrency, @NotNull Currency targetCurrency, @DecimalMin("0.5") @NotNull Double amount) {
}
