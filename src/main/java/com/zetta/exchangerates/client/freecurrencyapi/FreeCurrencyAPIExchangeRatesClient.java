package com.zetta.exchangerates.client.freecurrencyapi;

import com.zetta.exchangerates.client.freecurrencyapi.entity.FreeCurrencyAPIExchangeRatesResponse;
import com.zetta.exchangerates.client.freecurrencyapi.error.FreeCurrencyAPICustomFeignErrorDecoder;
import com.zetta.exchangerates.common.Constants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "exchange-rates-realtime-service",
             url = "${free_currency_api.realtime-url}?apikey=${free_currency_api.api-key}",
             configuration = FreeCurrencyAPICustomFeignErrorDecoder.class)
public interface FreeCurrencyAPIExchangeRatesClient {

    @GetMapping
    FreeCurrencyAPIExchangeRatesResponse getExchangeRates(@RequestParam(Constants.BASE_CURRENCY) String baseCurrency,
                                                          @RequestParam(Constants.TARGET_CURRENCIES) String targetCurrencies);
}
