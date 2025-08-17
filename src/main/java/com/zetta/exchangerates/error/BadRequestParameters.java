package com.zetta.exchangerates.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Function;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestParameters extends RuntimeException {

    protected BadRequestParameters(String message) {
        super(message);
    }

    public static BadRequestParameters rejectParams(String message, String... params) {
        return rejectParams(BadRequestParameters::new, message, params);
    }

    protected static <T extends BadRequestParameters> T rejectParams(Function<String, T> build, String message, String... params) {
        String paramList = "['" + String.join("','", params) + "']";
        return build.apply("%s! Rejected parameters are %s!".formatted(message, paramList));
    }
}
