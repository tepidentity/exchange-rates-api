package com.zetta.exchangerates.client.freecurrencyapi.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zetta.exchangerates.client.freecurrencyapi.FreeCurrencyAPIExchangeRatesClient;
import com.zetta.exchangerates.controller.ExchangeRatesController;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
            return new RuntimeException(
                    Optional.ofNullable(extractErrorBody(response))
                            .map(err -> err.errors()
                                                                .keySet()
                                                                .stream()
                                                                .map(this::serverToFreeCurrencyClientFieldMapping)
                                                                .filter(Objects::nonNull)
                                                                .map("Currency '%s' not supported!"::formatted)
                                                                .collect(Collectors.joining(", ")))
                            .orElse("Request rejected by FreeCurrency API"));
        } else {
            return errorDecoder.decode(methodKey, response);
        }
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
            case FreeCurrencyAPIExchangeRatesClient.BASE_CURRENCY -> ExchangeRatesController.SOURCE_CURRENCY;
            case FreeCurrencyAPIExchangeRatesClient.TARGET_CURRENCIES -> ExchangeRatesController.TARGET_CURRENCY;
            default -> null;
        };
    }
}