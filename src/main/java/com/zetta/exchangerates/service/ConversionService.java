package com.zetta.exchangerates.service;

import com.zetta.exchangerates.dto.ConversionDTO;
import com.zetta.exchangerates.dto.ConversionCompletionDTO;
import com.zetta.exchangerates.entity.Conversion;
import com.zetta.exchangerates.mapper.ConversionMapper;
import com.zetta.exchangerates.repository.ConversionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ConversionService {

    private final ConversionRepository conversionRepository;
    private final ExchangeRatesService exchangeRatesService;
    private final ConversionMapper conversionMapper;

    @Autowired
    public ConversionService(ConversionRepository conversionRepository,
                             ExchangeRatesService exchangeRatesService,
                             ConversionMapper conversionMapper) {
        this.conversionRepository = conversionRepository;
        this.exchangeRatesService = exchangeRatesService;
        this.conversionMapper = conversionMapper;
    }

    @Transactional
    public ConversionCompletionDTO convertAmount(ConversionDTO conversionDTO) {
        Double rate = exchangeRatesService.exchangeRate(conversionDTO.getSourceCurrency(), conversionDTO.getTargetCurrency())
                                          .exchangeRate();
        Double convertedAmount = convertAmount(conversionDTO.getAmount(), rate);
        Conversion conversion = conversionMapper.toEntity(conversionDTO, convertedAmount);
        conversion = conversionRepository.save(conversion);
        return conversionMapper.toDTO(conversion);
    }

    @Transactional(readOnly = true)
    public List<ConversionCompletionDTO> history(Date date, UUID transactionId, Pageable pageable) {
        List<Conversion> resultSet;
        if (date != null) {
            if (transactionId != null) {
                resultSet = conversionRepository.getOneByDateAndTransactionId(date, transactionId)
                                                .map(List::of)
                                                .orElseGet(Collections::emptyList);
            } else {
                resultSet = conversionRepository.findAllByDate(date, pageable);
            }
        } else {
            resultSet = conversionRepository.getOneByTransactionId(transactionId)
                                            .map(List::of)
                                            .orElseGet(Collections::emptyList);
        }
        return conversionMapper.toDTOs(resultSet);
    }

    public Double convertAmount(Double amount, Double rate) {
        BigDecimal temp = BigDecimal.valueOf(amount);
        temp = temp.multiply(BigDecimal.valueOf(rate));
        temp = new BigDecimal(temp.toString(), new MathContext(15, RoundingMode.FLOOR));
        return temp.doubleValue();
    }
}
