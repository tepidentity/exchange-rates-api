package com.zetta.exchangerates.service;

import com.zetta.exchangerates.dto.ExchangeRateDTO;
import com.zetta.exchangerates.entity.Currency;

public interface ExchangeRatesService {

    ExchangeRateDTO exchangeRate(Currency sourceCurrency, Currency targetCurrency);
}
