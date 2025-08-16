package com.zetta.exchangerates.controller;

import com.zetta.exchangerates.dto.ConversionCompletionDTO;
import com.zetta.exchangerates.dto.ConversionDTO;
import com.zetta.exchangerates.service.ConversionService;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/conversion")
@Validated
public class ConversionController {

    private final ConversionService conversionService;

    @Autowired
    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<ConversionCompletionDTO> convert(@RequestBody ConversionDTO conversion) {
        return ResponseEntity.ok(conversionService.convertAmount(conversion));
    }

    @GetMapping
    public ResponseEntity<List<ConversionCompletionDTO>> history(@RequestParam(value = "transaction_id", required = false) UUID transactionId,
                                                                 @RequestParam(value = "transaction_date", required = false)
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd") Date transactionDate,
                                                                 @RequestParam(value = "page", required = false, defaultValue="0") int page,
                                                                 @RequestParam(value = "pageSize", required = false, defaultValue="10") int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "date"));
        if (transactionDate == null && transactionId == null) {
            throw new IllegalArgumentException();
        }
        return ResponseEntity.ok(conversionService.history(transactionDate, transactionId, pageable));
    }
}
