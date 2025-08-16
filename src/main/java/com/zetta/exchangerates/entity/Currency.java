package com.zetta.exchangerates.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum Currency {

    EUR("Euro"),
    USD("US Dollar"),
    JPY("Japanese Yen"),
    BGN("Bulgarian Lev"),
    CZK("Czech Republic Koruna"),
    DKK("Danish Krone"),
    GBP("British Pound Sterling"),
    HUF("Hungarian Forint"),
    PLN("Polish Zloty"),
    RON("Romanian Leu"),
    SEK("Swedish Krona"),
    CHF("Swiss Franc"),
    ISK("Icelandic Króna"),
    NOK("Norwegian Krone"),
    HRK("Croatian Kuna"),
    RUB("Russian Ruble"),
    TRY("Turkish Lira"),
    AUD("Australian Dollar"),
    BRL("Brazilian Real"),
    CAD("Canadian Dollar"),
    CNY("Chinese Yuan"),
    HKD("Hong Kong Dollar"),
    IDR("Indonesian Rupiah"),
    ILS("Israeli New Sheqel"),
    INR("Indian Rupee"),
    KRW("South Korean Won"),
    MXN("Mexican Peso"),
    MYR("Malaysian Ringgit"),
    NZD("New Zealand Dollar"),
    PHP("Philippine Peso"),
    SGD("Singapore Dollar"),
    THB("Thai Baht"),
    ZAR("South African Rand");

    private final String description;

    public Currency fromName(String name) {
        return Arrays.stream(values())
                .filter(value -> Objects.equals(name.toLowerCase(), value.name().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public static List<String> shortCodes() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
