package com.zetta.exchangerates.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.zetta.exchangerates.config.WireMockConfig;
import com.zetta.exchangerates.dto.ConversionCompletionDTO;
import com.zetta.exchangerates.dto.ConversionDTO;
import com.zetta.exchangerates.entity.Conversion;
import com.zetta.exchangerates.entity.Currency;
import com.zetta.exchangerates.error.BadRequestParameters;
import com.zetta.exchangerates.repository.ConversionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.zetta.exchangerates.entity.Currency.CAD;
import static com.zetta.exchangerates.entity.Currency.USD;
import static com.zetta.exchangerates.testutils.TestUtils.buildExchangeResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
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
public class ConversionControllerIntegrationTest {

    private final ConversionController controller;
    private final ConversionRepository repository;
    private final WireMockServer client;
    private final String apiKey;

    @Autowired
    public ConversionControllerIntegrationTest(ConversionController controller,
                                               ConversionRepository repository,
                                               WireMockServer mockFreeCurrencyAPIExchangeRatesClient,
                                               @Value("${free_currency_api.api-key}") String apiKey) {
        this.controller = controller;
        this.repository = repository;
        this.client = mockFreeCurrencyAPIExchangeRatesClient;
        this.apiKey = apiKey;
    }

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @DisplayName("Conversion happy path")
    @Test
    public void conversion_happyPath() {
        // given
        var to = USD;
        var amount = 1d;
        var rate = 2d;
        var expected = amount * rate;

        String url = "/rates?base_currency=%s&currencies=%s&apikey=%s".formatted(CAD, to, apiKey);
        client.stubFor(WireMock.get(WireMock.urlEqualTo(url))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(buildExchangeResponse(Map.of(to.name(), rate)))));

        // when
        var result = controller.convert(new ConversionDTO(CAD, USD, amount));
        var body = result.getBody();

        // then
        assertNotNull(body);
        assertTrue(repository.getOneByTransactionId(body.transactionId()).isPresent());
        assertEquals(expected, body.amount());
    }

    @DisplayName("History by Date provided")
    @Test
    public void history_onlyDateProvided() {
        // given
        int lim = 5;
        int times = 3;
        LocalDate now = LocalDate.now();
        Iterator<Currency> cur = Arrays.asList(Currency.values()).iterator();
        List<Conversion> conversions = IntStream.rangeClosed(1, lim * times)
                                                .mapToObj(i -> {
                                                    Conversion result = new Conversion();
                                                    result.setSourceCurrency(cur.next());
                                                    result.setTargetCurrency(cur.next());
                                                    result.setSourceAmount(i * 1.0);
                                                    result.setTargetAmount(i * 1.5);
                                                    result = repository.save(result);
                                                    return result;
                                                }).toList();

        for (int step = 0; step < times; step++) {
            // when
            var result = controller.history(null, now, step + 1, lim);
            var body = result.getBody();

            // then
            assertNotNull(body);
            assertEquals(lim, body.size());
            assertAll(toAssertionList(conversions.stream().skip(step * lim).limit(lim).collect(Collectors.toSet()), body));
        }

        var result = controller.history(null, now, times + 1, lim);

        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }


    @DisplayName("History by TransactionId provided")
    @Test
    public void history_onlyIdProvided() {
        // given
        double amount = 2d;
        Conversion conversion = new Conversion();
        conversion.setSourceCurrency(USD);
        conversion.setTargetCurrency(CAD);
        conversion.setSourceAmount(1.0);
        conversion.setTargetAmount(amount);
        conversion = repository.save(conversion);
        UUID id = conversion.getTransactionId();

        // when
        var result = controller.history(id, null, 1, 10);
        var body = result.getBody();

        // then
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(amount, body.getFirst().amount());
        assertEquals(id, body.getFirst().transactionId());
    }

    @DisplayName("History by Date and TransactionId provided")
    @Test
    public void history_dateAndIdProvided() {
        // given
        double amount = 2d;
        Conversion conversion = new Conversion();
        conversion.setSourceCurrency(USD);
        conversion.setTargetCurrency(CAD);
        conversion.setSourceAmount(1.0);
        conversion.setTargetAmount(amount);
        conversion = repository.save(conversion);
        UUID id = conversion.getTransactionId();

        // when
        var result = controller.history(id, LocalDate.now(), 1, 10);
        var body = result.getBody();

        // then
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(amount, body.getFirst().amount());
        assertEquals(id, body.getFirst().transactionId());
    }

    @DisplayName("History returns empty list when no input provided")
    @Test
    public void history_noParamsProvided() {
        // when
        var ex = assertThrows(BadRequestParameters.class, () -> controller.history(null, null, 1, 10));

        // then
        assertThat(ex, instanceOf(BadRequestParameters.class));
    }

    private List<Executable> toAssertionList(Set<Conversion> expected, List<ConversionCompletionDTO> actual) {
        return expected.stream()
                       .map(c -> (Executable) () ->
                               assertTrue(actual.stream().anyMatch(cDto ->
                                       Objects.equals(c.getTransactionId(), cDto.transactionId())
                                       && Objects.equals(c.getTargetAmount(), cDto.amount()))))
                       .toList();
    }
}
