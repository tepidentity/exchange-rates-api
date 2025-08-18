package com.zetta.exchangerates.service;

import com.zetta.exchangerates.client.freecurrencyapi.FreeCurrencyAPIExchangeRatesClient;
import com.zetta.exchangerates.client.freecurrencyapi.entity.FreeCurrencyAPIExchangeRatesResponse;
import com.zetta.exchangerates.error.BadRequestParameters;
import com.zetta.exchangerates.error.ExchangeRatesAPIParamsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static com.zetta.exchangerates.common.Constants.TARGET_CURRENCY;
import static com.zetta.exchangerates.entity.Currency.CAD;
import static com.zetta.exchangerates.entity.Currency.USD;
import static com.zetta.exchangerates.error.ExchangeRatesAPIParamsException.FIELD_SPECIFIC_ERROR_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FreeCurrencyAPIExchangeRatesServiceTest {

    @Mock
    private FreeCurrencyAPIExchangeRatesClient client;

    @InjectMocks
    private FreeCurrencyAPIExchangeRatesService service;

    @DisplayName("Conversion happy path")
    @Test
    public void exchangeRate_happyPath() {
        // given
        var from = CAD;
        var to = USD;
        double amount = 1d;
        when(client.getExchangeRates(eq(from.name()), eq(to.name()))).thenReturn(
                new FreeCurrencyAPIExchangeRatesResponse(Map.of(to.name(), amount)));

        // when
        var result = service.exchangeRate(from, to);

        // then
        assertEquals(amount, result.exchangeRate());
    }

    @DisplayName("Conversion fails for missing currency in response")
    @Test
    public void exchangeRate_failsOnMissingCurrencyInResponse() {
        // given
        var from = CAD;
        var to = USD;
        when(client.getExchangeRates(eq(from.name()), eq(to.name()))).thenReturn(
                new FreeCurrencyAPIExchangeRatesResponse(Collections.emptyMap()));

        // when
        var ex = assertThrows(ExchangeRatesAPIParamsException.class, () -> service.exchangeRate(from, to));

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIParamsException.class));
        assertThat(ex.getMessage(), startsWith(FIELD_SPECIFIC_ERROR_MESSAGE));
        assertThat(ex.getMessage(), containsString(TARGET_CURRENCY));
    }

    @DisplayName("Conversion fails for unsupported currency")
    @Test
    public void exchangeRate_failsOnUnknownCurrency() {
        // given
        var from = CAD;
        var to = USD;
        when(client.getExchangeRates(eq(from.name()), eq(to.name()))).thenThrow(ExchangeRatesAPIParamsException.class);

        // when
        var ex = assertThrows(ExchangeRatesAPIParamsException.class, () -> service.exchangeRate(from, to));

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIParamsException.class));
    }

    @DisplayName("Conversion fails for same source and target currency")
    @Test
    public void exchangeRate_sameSourceAndTargetCurrency() {
        // given
        var from = CAD;

        // when
        var ex = assertThrows(BadRequestParameters.class, () -> service.exchangeRate(from, from));

        // then
        assertThat(ex, instanceOf(BadRequestParameters.class));
    }
}
