package com.zetta.exchangerates.controller;

import com.zetta.exchangerates.dto.ConversionCompletionDTO;
import com.zetta.exchangerates.dto.ConversionDTO;
import com.zetta.exchangerates.error.BadRequestParameters;
import com.zetta.exchangerates.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.zetta.exchangerates.common.Constants.TRANSACTION_DATE;
import static com.zetta.exchangerates.common.Constants.TRANSACTION_ID;

@RestController
@RequestMapping("/conversion")
@Validated
@Tag(name = "Currency Conversion API")
public class ConversionController {
    private final ConversionService conversionService;

    @Autowired
    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Operation(summary = "Convert currency by providing base and target currencies and amount to be convert.",
               description = "Returns the resulting amount in target currency.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency successfully converted.",
                         content = @Content(schema = @Schema(implementation = ConversionDTO.class))),
            @ApiResponse(responseCode = "400",
                         description = "Either base and/or target currency is invalid (unsupported), or Base and Target currencies should differ.",
                         content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "422", description = "Currency Exchange Rates API no longer supports base and/or target currencies.",
                         content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "503", description = "Currency Exchange Rates API currently unavailable.",
                         content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ResponseEntity<ConversionCompletionDTO> convert(@Valid @RequestBody ConversionDTO conversion) {
        return ResponseEntity.ok(conversionService.convertAmount(conversion));
    }

    @Operation(summary = "Retrieve list of historical conversion transactions. Supports pagination - by default first page index is 1 and page size is 10",
               description = "Returns a page of conversion transactions in chronological order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency successfully converted.",
                    content = @Content(schema = @Schema(implementation = ConversionDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Either TransactionId and/or TransactionDate should be provided.",
                    content = @Content(schema = @Schema()))
    })
    @Parameters({
            @Parameter(name = "transactionDate", schema = @Schema(type = "string", pattern = "yyyy-MM-dd", example = "2025-01-01"))
    })
    @GetMapping
    public ResponseEntity<List<ConversionCompletionDTO>> history(@RequestParam(value = TRANSACTION_ID, required = false) UUID transactionId,
                                                                 @RequestParam(value = TRANSACTION_DATE, required = false)
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate transactionDate,
                                                                 @RequestParam(value = "page", required = false, defaultValue="1") int page,
                                                                 @RequestParam(value = "pageSize", required = false, defaultValue="10") int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, Sort.by(Sort.Direction.ASC, "id"));
        if (transactionDate == null && transactionId == null) {
            throw BadRequestParameters.rejectParams("At least on of the fields is mandatory", TRANSACTION_ID, TRANSACTION_DATE);
        }
        return ResponseEntity.ok(conversionService.history(transactionDate, transactionId, pageable));
    }
}
