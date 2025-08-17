package com.zetta.exchangerates.client.freecurrencyapi.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zetta.exchangerates.client.freecurrencyapi.FreeCurrencyAPIExchangeRatesClient;
import com.zetta.exchangerates.error.ExchangeRatesAPIParamsException;
import com.zetta.exchangerates.error.ExchangeRatesAPIRequestException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.zetta.exchangerates.common.Constants.SOURCE_CURRENCY;
import static com.zetta.exchangerates.common.Constants.TARGET_CURRENCY;

@Component
public class FreeCurrencyAPICustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new ErrorDecoder.Default();
    private final ObjectMapper objectMapper;

    @Autowired
    public FreeCurrencyAPICustomFeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        // Map status codes to exceptions
        if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
            ValidationErrorResponse errorBody = extractErrorBody(response);
            String[] fields = errorBody.errors()
                    .keySet()
                    .stream()
                    .map(this::serverToFreeCurrencyClientFieldMapping)
                    .filter(Objects::nonNull)
                    .toArray(String[]::new);
            if (fields.length > 0) {
                return ExchangeRatesAPIParamsException.rejectClientParams(fields);
            }
            return ExchangeRatesAPIParamsException.rejectedRequest();
        }
        Exception defaultError = errorDecoder.decode(methodKey, response);
        if (defaultError instanceof RetryableException) {
            return defaultError;
        }
        return new ExchangeRatesAPIRequestException(defaultError);
    }

    private ValidationErrorResponse extractErrorBody(Response response) {
        if (response == null || response.body() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8),
                                          ValidationErrorResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private String serverToFreeCurrencyClientFieldMapping(String field) {
        return switch(field) {
            case FreeCurrencyAPIExchangeRatesClient.BASE_CURRENCY -> SOURCE_CURRENCY;
            case FreeCurrencyAPIExchangeRatesClient.TARGET_CURRENCIES -> TARGET_CURRENCY;
            default -> null;
        };
    }
}