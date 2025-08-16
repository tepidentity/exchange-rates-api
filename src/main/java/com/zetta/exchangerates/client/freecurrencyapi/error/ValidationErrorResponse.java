package com.zetta.exchangerates.client.freecurrencyapi.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ValidationErrorResponse(String message, Map<String, List<String>> errors) {
}
