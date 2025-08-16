package com.zetta.exchangerates.dto;

import java.util.UUID;

public record ConversionCompletionDTO(UUID transactionId, Double amount) {
}
