package com.banking.carddelivery.util;

import org.springframework.stereotype.Component;

@Component
public class CustomerIdMasker {

    private static final int VISIBLE_PREFIX_LENGTH = 3;

    public String mask(String customerId) {
        if (customerId == null || customerId.length() <= VISIBLE_PREFIX_LENGTH) {
            return "***";
        }
        return customerId.substring(0, VISIBLE_PREFIX_LENGTH) + "***";
    }
}

