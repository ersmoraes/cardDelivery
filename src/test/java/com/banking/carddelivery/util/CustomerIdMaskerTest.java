package com.banking.carddelivery.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerIdMaskerTest {

    private final CustomerIdMasker masker = new CustomerIdMasker();

    @Test
    void mask_normalId_showsPrefix() {
        assertThat(masker.mask("C123456")).isEqualTo("C12***");
    }

    @Test
    void mask_longId_showsOnlyFirstThreeChars() {
        assertThat(masker.mask("C123456789")).isEqualTo("C12***");
    }

    @Test
    void mask_shortId_returnsStars() {
        assertThat(masker.mask("C1")).isEqualTo("***");
    }

    @Test
    void mask_exactlyThreeChars_returnsStars() {
        assertThat(masker.mask("C12")).isEqualTo("***");
    }

    @Test
    void mask_null_returnsStars() {
        assertThat(masker.mask(null)).isEqualTo("***");
    }

    @Test
    void mask_fourChars_showsPrefix() {
        assertThat(masker.mask("C123")).isEqualTo("C12***");
    }
}
