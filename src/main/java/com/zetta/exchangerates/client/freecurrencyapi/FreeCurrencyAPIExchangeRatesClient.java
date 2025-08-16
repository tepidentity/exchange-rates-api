package com.zetta.exchangerates.client.freecurrencyapi;

import com.zetta.exchangerates.client.freecurrencyapi.entity.FreeCurrencyAPIExchangeRatesResponse;
import com.zetta.exchangerates.client.freecurrencyapi.error.FreeCurrencyAPICustomFeignErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "exchange-rates-realtime-service",
             url = "${free_currency_api.realtime-url}?apikey=${free_currency_api.api-key}",
             configuration = FreeCurrencyAPICustomFeignErrorDecoder.class)
public interface FreeCurrencyAPIExchangeRatesClient {

    String BASE_CURRENCY = "base_currency";
    String TARGET_CURRENCIES = "currencies";

    @GetMapping
    FreeCurrencyAPIExchangeRatesResponse getExchangeRates(@RequestParam(BASE_CURRENCY) String baseCurrency,
                                                          @RequestParam(TARGET_CURRENCIES) String targetCurrencies);
}
