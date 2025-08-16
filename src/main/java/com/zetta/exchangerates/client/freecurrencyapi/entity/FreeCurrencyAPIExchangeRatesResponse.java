package com.zetta.exchangerates.client.freecurrencyapi.entity;

import java.util.Map;

public record FreeCurrencyAPIExchangeRatesResponse(Map<String, Double> data) {
}
