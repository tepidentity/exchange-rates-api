package com.zetta.exchangerates.service;

import com.zetta.exchangerates.client.freecurrencyapi.FreeCurrencyAPIExchangeRatesClient;
import com.zetta.exchangerates.dto.ExchangeRateDTO;
import com.zetta.exchangerates.entity.Currency;
import com.zetta.exchangerates.error.ExchangeRatesAPIParamsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.zetta.exchangerates.common.Constants.TARGET_CURRENCY;

@Service
public class FreeCurrencyAPIExchangeRatesService implements ExchangeRatesService {

    private final FreeCurrencyAPIExchangeRatesClient freeCurrencyAPIExchangeRatesClient;

    @Autowired
    public FreeCurrencyAPIExchangeRatesService(FreeCurrencyAPIExchangeRatesClient freeCurrencyAPIExchangeRatesClient) {
        this.freeCurrencyAPIExchangeRatesClient = freeCurrencyAPIExchangeRatesClient;
    }

    @Override
    public ExchangeRateDTO exchangeRate(Currency sourceCurrency, Currency targetCurrency) {
        return Optional.ofNullable(freeCurrencyAPIExchangeRatesClient.getExchangeRates(sourceCurrency.name(), targetCurrency.name())
                                                                     .data()
                                                                     .get(targetCurrency.name()))
                       .map(ExchangeRateDTO::new)
                       .orElseThrow(() -> ExchangeRatesAPIParamsException.rejectClientParams(TARGET_CURRENCY));
    }
}
