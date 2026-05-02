package com.banking.carddelivery.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CepValidator {

    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

    public boolean isValid(String cep) {
        return cep != null && CEP_PATTERN.matcher(cep).matches();
    }
}

