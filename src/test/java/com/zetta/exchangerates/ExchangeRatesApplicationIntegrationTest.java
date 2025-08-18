package com.zetta.exchangerates;

import com.zetta.exchangerates.repository.ConversionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ExchangeRatesApplicationIntegrationTest {

    private final ConversionRepository repository;

    @Autowired
    ExchangeRatesApplicationIntegrationTest(ConversionRepository repository) {
        this.repository = repository;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void dbAccessible() {
        assertEquals(0, repository.count());
    }
}