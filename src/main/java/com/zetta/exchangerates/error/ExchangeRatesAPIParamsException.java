package com.zetta.exchangerates.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ExchangeRatesAPIParamsException extends BadRequestParameters {

    protected ExchangeRatesAPIParamsException(String message) {
        super(message);
    }

    public static ExchangeRatesAPIParamsException rejectedRequest() {
        return new ExchangeRatesAPIParamsException("ExchangeRatesAPI rejected the request!");
    }

    public static ExchangeRatesAPIParamsException rejectClientParams(String... params) {
        return rejectParams(ExchangeRatesAPIParamsException::new, "ExchangeRatesAPI rejected parameter values", params);
    }
}
