package com.zetta.exchangerates.mapper;

import com.zetta.exchangerates.dto.ConversionCompletionDTO;
import com.zetta.exchangerates.dto.ConversionDTO;
import com.zetta.exchangerates.entity.Conversion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversionMapper {

    @Mapping(target = "sourceCurrency", source = "dto.sourceCurrency")
    @Mapping(target = "targetCurrency", source = "dto.targetCurrency")
    @Mapping(target = "sourceAmount", source = "dto.amount")
    @Mapping(target = "targetAmount", source = "convertedAmount")
    Conversion toEntity(ConversionDTO dto, Double convertedAmount);

    @Mapping(target = "amount", source = "targetAmount")
    ConversionCompletionDTO toDTO(Conversion conversion);

    List<ConversionCompletionDTO> toDTOs(List<Conversion> conversions);
}
