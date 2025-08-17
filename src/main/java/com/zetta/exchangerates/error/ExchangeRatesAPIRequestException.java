package com.zetta.exchangerates.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ExchangeRatesAPIRequestException extends RuntimeException {

    public ExchangeRatesAPIRequestException(Throwable cause) {
        super("ExchangeRatesAPI currently unavailable!", cause);
    }
}
