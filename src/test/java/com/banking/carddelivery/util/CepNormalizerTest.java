package com.banking.carddelivery.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CepNormalizerTest {

    private final CepNormalizer normalizer = new CepNormalizer();

    @Test
    void normalize_withDash_removesDash() {
        assertThat(normalizer.normalize("01310-100")).isEqualTo("01310100");
    }

    @Test
    void normalize_withoutDash_unchanged() {
        assertThat(normalizer.normalize("01310100")).isEqualTo("01310100");
    }

    @Test
    void normalize_null_returnsNull() {
        assertThat(normalizer.normalize(null)).isNull();
    }

    @Test
    void normalize_multipleDashes_removesAll() {
        assertThat(normalizer.normalize("013-10-100")).isEqualTo("01310100");
    }
}
