package com.zetta.exchangerates.client.freecurrencyapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FreeCurrencyAPIExchangeRatesResponse(Map<String, Double> data) {
}
