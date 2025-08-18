package com.zetta.exchangerates.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ExchangeRatesAPIRequestException extends RuntimeException {
    public static final String GENERIC_ERROR_MESSAGE = "ExchangeRatesAPI currently unavailable!";

    public ExchangeRatesAPIRequestException(Throwable cause) {
        super(GENERIC_ERROR_MESSAGE, cause);
    }
}
