package com.banking.carddelivery.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CepValidatorTest {

    private final CepValidator validator = new CepValidator();

    @Test
    void isValid_withDash_returnsTrue() {
        assertThat(validator.isValid("01310-100")).isTrue();
    }

    @Test
    void isValid_withoutDash_returnsTrue() {
        assertThat(validator.isValid("01310100")).isTrue();
    }

    @Test
    void isValid_null_returnsFalse() {
        assertThat(validator.isValid(null)).isFalse();
    }

    @Test
    void isValid_empty_returnsFalse() {
        assertThat(validator.isValid("")).isFalse();
    }

    @Test
    void isValid_tooShort_returnsFalse() {
        assertThat(validator.isValid("1234567")).isFalse();
    }

    @Test
    void isValid_withLetters_returnsFalse() {
        assertThat(validator.isValid("AB310-100")).isFalse();
    }

    @Test
    void isValid_tooLong_returnsFalse() {
        assertThat(validator.isValid("013101000")).isFalse();
    }
}
