package com.zetta.exchangerates.controller;

import com.zetta.exchangerates.dto.ExchangeRateDTO;
import com.zetta.exchangerates.entity.Currency;
import com.zetta.exchangerates.service.ExchangeRatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Currency Exchange Rates API")
public class ExchangeRatesController {

    private final ExchangeRatesService exchangeRatesService;

    @Autowired
    public ExchangeRatesController(ExchangeRatesService exchangeRatesService) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @Operation(summary = "Get currency exchange rate for base and target currencies.",
            description = "Returns real time exchange rate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved",
                         content = @Content(schema = @Schema(implementation = ExchangeRateDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Either base and/or target currency is invalid (unsupported), or Base and Target currencies should differ.",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "422", description = "Currency Exchange Rates API no longer supports base and/or target currencies.",
                         content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "503", description = "Currency Exchange Rates API currently unavailable.",
                         content = @Content(schema = @Schema()))
    })
    @Parameters(value = {
            @Parameter(name = "sourceCurrency", schema = @Schema(implementation = Currency.class)),
            @Parameter(name = "targetCurrency", schema = @Schema(implementation = Currency.class))
    })
    @GetMapping("/exchangeRate")
    public ResponseEntity<ExchangeRateDTO> getExchangeRate(@RequestParam(SOURCE_CURRENCY) Currency sourceCurrency,
                                                           @RequestParam(TARGET_CURRENCY) Currency targetCurrency) {
        return ResponseEntity.ok(exchangeRatesService.exchangeRate(sourceCurrency, targetCurrency));
    }


    @Operation(summary = "List all known currencies shortcodes.",
            description = "Returns real time exchange rate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved",
                         content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    })
    @GetMapping("/shortCodes")
    public ResponseEntity<List<String>> shortcodesList() {
        return ResponseEntity.ok(Currency.shortCodes());
    }
}
