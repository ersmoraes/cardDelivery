package com.banking.carddelivery.util;

import org.springframework.stereotype.Component;

@Component
public class CepNormalizer {

    public String normalize(String cep) {
        if (cep == null) {
            return null;
        }
        return cep.replace("-", "");
    }
}
