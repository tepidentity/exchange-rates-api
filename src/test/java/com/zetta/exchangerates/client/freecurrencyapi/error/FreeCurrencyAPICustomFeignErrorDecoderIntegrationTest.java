package com.zetta.exchangerates.client.freecurrencyapi.error;

import com.zetta.exchangerates.common.Constants;
import com.zetta.exchangerates.error.ExchangeRatesAPIParamsException;
import com.zetta.exchangerates.error.ExchangeRatesAPIRequestException;
import com.zetta.exchangerates.testutils.TestUtils;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@SpringBootTest
public class FreeCurrencyAPICustomFeignErrorDecoderIntegrationTest {

    @Autowired
    private FreeCurrencyAPICustomFeignErrorDecoder decoder;

    @DisplayName("Specified rejected parameter names for all known parameters")
    @Test
    public void listsRejectedParameters() throws IOException {
        // given
        Response response = mock(Response.class);
        Response.Body body = mock(Response.Body.class);
        when(response.status()).thenReturn(UNPROCESSABLE_ENTITY.value());
        when(response.body()).thenReturn(body);

        String json = TestUtils.jsonAsString("InvalidClientRequest_AllParamsKnown.json");
        when(body.asInputStream()).thenReturn(new ByteArrayInputStream(json.getBytes()));

        // when
        Exception ex = decoder.decode("ignored", response);

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIParamsException.class));
        assertThat(ex.getMessage(), startsWith(ExchangeRatesAPIParamsException.FIELD_SPECIFIC_ERROR_MESSAGE));
        assertThat(ex.getMessage(), containsString(Constants.SOURCE_CURRENCY));
        assertThat(ex.getMessage(), containsString(Constants.TARGET_CURRENCY));
    }

    @DisplayName("Specified rejected parameter names only for known parameters")
    @Test
    public void listsOnlyKnownParameters() throws IOException {
        // given
        Response response = mock(Response.class);
        Response.Body body = mock(Response.Body.class);
        when(response.status()).thenReturn(UNPROCESSABLE_ENTITY.value());
        when(response.body()).thenReturn(body);

        String json = TestUtils.jsonAsString("InvalidClientRequest_SomeParamsKnown.json");
        when(body.asInputStream()).thenReturn(new ByteArrayInputStream(json.getBytes()));

        // when
        Exception ex = decoder.decode("ignored", response);

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIParamsException.class));
        assertThat(ex.getMessage(), startsWith(ExchangeRatesAPIParamsException.FIELD_SPECIFIC_ERROR_MESSAGE));
        assertThat(ex.getMessage(), containsString(Constants.TARGET_CURRENCY));
    }

    @DisplayName("Shows generic message on stats code 422 and unknown rejected parameter names")
    @Test
    public void genericExceptionMessageForUnknownParameters() throws IOException {
        // given
        Response response = mock(Response.class);
        Response.Body body = mock(Response.Body.class);
        when(response.status()).thenReturn(UNPROCESSABLE_ENTITY.value());
        when(response.body()).thenReturn(body);

        String json = TestUtils.jsonAsString("InvalidClientRequest_NoKnownParameters.json");
        when(body.asInputStream()).thenReturn(new ByteArrayInputStream(json.getBytes()));

        // when
        Exception ex = decoder.decode("ignored", response);

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIParamsException.class));
        assertThat(ex.getMessage(), startsWith(ExchangeRatesAPIParamsException.GENERIC_ERROR_MESSAGE));
    }


    @DisplayName("Shows generic message on status codes different than 422")
    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404, 429, 500})
    public void genericExceptionMessageForOtherStatusCodes(int code) {
        // given
        Response response = mock(Response.class);
        when(response.status()).thenReturn(BAD_REQUEST.value());
        when(response.request()).thenReturn(mock(Request.class));

        // when
        Exception ex = decoder.decode("ignored", response);

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIRequestException.class));
        assertThat(ex.getMessage(), startsWith(ExchangeRatesAPIRequestException.GENERIC_ERROR_MESSAGE));
    }
}
