package com.zetta.exchangerates.controller;

import com.zetta.exchangerates.dto.ExchangeRateDTO;
import com.zetta.exchangerates.entity.Currency;
import com.zetta.exchangerates.service.ExchangeRatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.zetta.exchangerates.common.Constants.SOURCE_CURRENCY;
import static com.zetta.exchangerates.common.Constants.TARGET_CURRENCY;

@RestController
@RequestMapping("/currency")
@Validated
public class ExchangeRatesController {

    private final ExchangeRatesService exchangeRatesService;

    @Autowired
    public ExchangeRatesController(ExchangeRatesService exchangeRatesService) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @GetMapping("/exchangeRate")
    public ResponseEntity<ExchangeRateDTO> getExchangeRate(@RequestParam(SOURCE_CURRENCY) Currency sourceCurrency,
                                                           @RequestParam(TARGET_CURRENCY) Currency targetCurrency) {
        return ResponseEntity.ok(exchangeRatesService.exchangeRate(sourceCurrency, targetCurrency));
    }

    @GetMapping("/shortCodes")
    public ResponseEntity<List<String>> shortcodesList() {
        return ResponseEntity.ok(Currency.shortCodes());
    }
}
