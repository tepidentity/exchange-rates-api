package com.zetta.exchangerates.service;

import com.zetta.exchangerates.dto.ConversionCompletionDTO;
import com.zetta.exchangerates.dto.ConversionDTO;
import com.zetta.exchangerates.dto.ExchangeRateDTO;
import com.zetta.exchangerates.entity.Conversion;
import com.zetta.exchangerates.error.ExchangeRatesAPIParamsException;
import com.zetta.exchangerates.mapper.ConversionMapper;
import com.zetta.exchangerates.repository.ConversionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.zetta.exchangerates.entity.Currency.CAD;
import static com.zetta.exchangerates.entity.Currency.USD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConversionServiceTest {

    @Mock
    private ExchangeRatesService exchangeRatesService;
    @Spy
    private ConversionMapper mapper = Mappers.getMapper(ConversionMapper.class);
    @Mock
    private ConversionRepository repository;

    @InjectMocks
    private ConversionService service;

    @DisplayName("Conversion happy path")
    @Test
    public void conversion_happyPath() {
        // given
        var from = CAD;
        var to = USD;
        var amount = 1d;
        var rate = 2d;
        var expected = amount * rate;
        var id = UUID.randomUUID();
        var dto = new ConversionDTO(CAD, USD, amount);
        var rateDto = new ExchangeRateDTO(rate);

        when(exchangeRatesService.exchangeRate(eq(from), eq(to))).thenReturn(rateDto);
        when(repository.save(any())).thenAnswer(invocation -> {
            var arg0 = (Conversion) invocation.getArgument(0);
            arg0.setTransactionId(id);
            return arg0;
        });

        // when
        var result = service.convertAmount(dto);

        // then
        assertEquals(expected, result.amount());
        assertEquals(id, result.transactionId());
        verify(exchangeRatesService, times(1)).exchangeRate(eq(from), eq(to));
        verify(mapper, times(1)).toEntity(eq(dto), eq(expected));
        verify(repository, times(1)).save(assertArg(arg -> {
            assertEquals(from, arg.getSourceCurrency());
            assertEquals(to, arg.getTargetCurrency());
            assertEquals(amount, arg.getSourceAmount());
            assertEquals(expected, arg.getTargetAmount());
        }));
        verify(mapper, times(1)).toDTO(assertArg(arg -> {
            assertNotNull(arg.getTransactionId());
            assertNotNull(arg.getTargetAmount());
        }));
    }

    @DisplayName("Fails for unsupported currency")
    @Test
    public void conversion_failsForUnsupportedCurrency() {
        // given
        var dto = new ConversionDTO(USD, USD, 1.0);

        when(exchangeRatesService.exchangeRate(any(), any())).thenThrow(ExchangeRatesAPIParamsException.class);

        // when
        var ex = assertThrows(ExchangeRatesAPIParamsException.class, () -> service.convertAmount(dto));

        // then
        assertThat(ex, instanceOf(ExchangeRatesAPIParamsException.class));
        verify(mapper, never()).toEntity(any(), anyDouble());
        verify(repository, never()).save(any());
        verify(mapper, never()).toDTO(any());
    }

    @DisplayName("History by Date provided")
    @Test
    public void history_onlyDateProvided() {
        // given
        int max = 5;
        Pageable page = mock(Pageable.class);
        LocalDate now = LocalDate.now();
        List<Conversion> historyList = IntStream.range(0, max).mapToObj(_ -> mock(Conversion.class)).toList();
        List<ConversionCompletionDTO> expected = IntStream.range(0, max).mapToObj(_ -> mock(ConversionCompletionDTO.class)).toList();

        when(repository.findAllByDate(eq(now), eq(page))).thenReturn(historyList);
        when(mapper.toDTOs(eq(historyList))).thenReturn(expected);

        // when
        var result = service.history(now, null, page);

        // then
        verify(repository, never()).getOneByTransactionId(any());
        verify(repository, never()).getOneByDateAndTransactionId(any(), any());
        assertEquals(max, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("History by TransactionId provided")
    @Test
    public void history_onlyIdProvided() {
        // given
        double amount = 2d;
        UUID transactionId = UUID.randomUUID();
        Pageable page = mock(Pageable.class);
        Conversion history = mock(Conversion.class);
        when(history.getTargetAmount()).thenReturn(amount);
        when(history.getTransactionId()).thenReturn(transactionId);

        when(repository.getOneByTransactionId(eq(transactionId))).thenReturn(Optional.of(history));

        // when
        var result = service.history(null, transactionId, page);

        // then
        verify(repository, never()).findAllByDate(any(), any());
        verify(repository, never()).getOneByDateAndTransactionId(any(), any());
        assertEquals(1, result.size());
        assertEquals(amount, result.getFirst().amount());
        assertEquals(transactionId, result.getFirst().transactionId());
    }

    @DisplayName("History by Date and TransactionId provided")
    @Test
    public void history_dateAndIdProvided() {
        // given
        double amount = 2d;
        UUID transactionId = UUID.randomUUID();
        Pageable page = mock(Pageable.class);
        LocalDate now = LocalDate.now();
        Conversion history = mock(Conversion.class);
        when(history.getTargetAmount()).thenReturn(amount);
        when(history.getTransactionId()).thenReturn(transactionId);

        when(repository.getOneByDateAndTransactionId(eq(now), eq(transactionId))).thenReturn(Optional.of(history));

        // when
        var result = service.history(now, transactionId, page);

        // then
        verify(repository, never()).findAllByDate(any(), any());
        verify(repository, never()).getOneByTransactionId(any());
        assertEquals(1, result.size());
        assertEquals(amount, result.getFirst().amount());
        assertEquals(transactionId, result.getFirst().transactionId());
    }

    @DisplayName("History returns empty list when no input provided")
    @Test
    public void history_noParamsProvided() {
        // given

        // when
        var result = service.history(null, null, null);

        // then
        verify(repository, never()).findAllByDate(any(), any());
        verify(repository, never()).getOneByTransactionId(any());
        verify(repository, never()).getOneByDateAndTransactionId(any(), any());
        assertTrue(result.isEmpty());
    }
}
