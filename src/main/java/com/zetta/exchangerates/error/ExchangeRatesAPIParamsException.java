package com.zetta.exchangerates.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ExchangeRatesAPIParamsException extends BadRequestParameters {
    public static final String GENERIC_ERROR_MESSAGE = "ExchangeRatesAPI rejected the request!";
    public static final String FIELD_SPECIFIC_ERROR_MESSAGE = "ExchangeRatesAPI rejected parameter values";

    protected ExchangeRatesAPIParamsException(String message) {
        super(message);
    }

    public static ExchangeRatesAPIParamsException rejectedRequest() {
        return new ExchangeRatesAPIParamsException(GENERIC_ERROR_MESSAGE);
    }

    public static ExchangeRatesAPIParamsException rejectClientParams(String... params) {
        return rejectParams(ExchangeRatesAPIParamsException::new, FIELD_SPECIFIC_ERROR_MESSAGE, params);
    }
}
