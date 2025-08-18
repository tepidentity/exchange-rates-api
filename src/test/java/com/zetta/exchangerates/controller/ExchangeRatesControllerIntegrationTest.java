package com.zetta.exchangerates.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.zetta.exchangerates.entity.Currency;
import com.zetta.exchangerates.config.WireMockConfig;
import com.zetta.exchangerates.error.BadRequestParameters;
import com.zetta.exchangerates.error.ExchangeRatesAPIParamsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zetta.exchangerates.common.Constants.TARGET_CURRENCY;
import static com.zetta.exchangerates.entity.Currency.CAD;
import static com.zetta.exchangerates.entity.Currency.USD;
import static com.zetta.exchangerates.error.ExchangeRatesAPIParamsException.FIELD_SPECIFIC_ERROR_MESSAGE;
import static com.zetta.exchangerates.testutils.TestUtils.buildExchangeResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@EnableFeignClients
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WireMockConfig.class })
public class ExchangeRatesControllerIntegrationTest {

    private final WireMockServer client;
    private final ExchangeRatesController controller;
    private final String apiKey;

    @Autowired
    public ExchangeRatesControllerIntegrationTest(ExchangeRatesController controller,
                                                  WireMockServer mockFreeCurrencyAPIExchangeRatesClient,
                                                  @Value("${free_currency_api.api-key}") String apiKey) {
        this.controller = controller;
        this.client = mockFreeCurrencyAPIExchangeRatesClient;
        this.apiKey = apiKey;
    }

    @DisplayName("Conversion happy path")
    @Test
    public void exchangeRate_happyPath() {
        // given
        Currency from = USD;
        Currency to = CAD;
        var expected = 10d;

        String url = "/rates?base_currency=%s&currencies=%s&apikey=%s".formatted(from, to, apiKey);
        client.stubFor(WireMock.get(WireMock.urlEqualTo(url))
                               .willReturn(WireMock.aResponse()
                               .withStatus(HttpStatus.OK.value())
                               .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                               .withBody(buildExchangeResponse(Map.of(to.name(), expected)))));

        // when
        var result = controller.getExchangeRate(from, to);
        var rate = result.getBody();

        // then
        assertNotNull(rate);
        assertEquals(expected, rate.exchangeRate());
    }

    @DisplayName("Conversion fails for missing currency in response")
    @Test
    public void exchangeRate_failsOnMissingCurrencyInResponse() {
        // given
        Currency from = USD;
        Currency to = CAD;

        String url = "/rates?base_currency=%s&currencies=%s&apikey=%s".formatted(from, to, apiKey);
        client.stubFor(WireMock.get(WireMock.urlEqualTo(url))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(buildExchangeResponse(Map.of("Unknown", 1d)))));

        // when
        var ex = assertThrows(ExchangeRatesAPIParamsException.class, () -> controller.getExchangeRate(from, to));

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIParamsException.class));
        assertThat(ex.getMessage(), startsWith(FIELD_SPECIFIC_ERROR_MESSAGE));
        assertThat(ex.getMessage(), containsString(TARGET_CURRENCY));
    }

    @DisplayName("Conversion fails for same source and target currency")
    @Test
    public void exchangeRate_sameSourceAndTargetCurrency() {
         // when
        var ex = assertThrows(BadRequestParameters.class, () -> controller.getExchangeRate(USD, USD));

        // then
        assertThat(ex, instanceOf(BadRequestParameters.class));
    }

    @DisplayName("Exact match of known currency values is returned to end user")
    @Test
    public void currencyCodes_allKnownValuesAreAvailable() {
        // given
        var expected = Stream.of(Currency.values()).map(Enum::name).collect(Collectors.toSet());

        // when
        var result = controller.shortcodesList();
        var list = result.getBody();

        // then
        assertNotNull(list);
        assertEquals(expected.size(), list.size());
        assertTrue(list.containsAll(expected));
    }
}
