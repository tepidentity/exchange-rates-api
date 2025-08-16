package com.zetta.exchangerates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zetta.exchangerates.client.freecurrencyapi.error.ValidationErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExchangeRatesApplicationTestsDTO {

    @Test
    void contextLoads() {
    }

    @Test
    void other() throws JsonProcessingException {
        String s = """
                {
                  "message" : "Validation error",
                  "errors" : {
                    "base_currency" : [ "The selected base currency is invalid." ],
                    "currencies" : [ "The selected currencies is invalid." ]
                  },
                  "info" : "For more information, see documentation: https://freecurrencyapi.com/docs/status-codes#_422"
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();
        ValidationErrorResponse ss = objectMapper.readValue(s, ValidationErrorResponse.class);
        System.out.println(ss);
    }
}
