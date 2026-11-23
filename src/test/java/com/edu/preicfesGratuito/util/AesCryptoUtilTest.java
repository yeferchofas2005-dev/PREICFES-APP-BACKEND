package com.edu.preicfesGratuito.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesCryptoUtilTest {

    private final AesCryptoUtil crypto = new AesCryptoUtil("clave-secreta-pruebas-rumbo-a-la-u-2026");

    @Test
    void encryptYDecrypt_recuperanTextoOriginalConIvAleatorio() {
        String original = "ClaveTemporal2026!";

        String cifrado1 = crypto.encrypt(original);
        String cifrado2 = crypto.encrypt(original);

        assertThat(cifrado1).isNotBlank().isNotEqualTo(original);
        assertThat(cifrado2).isNotEqualTo(cifrado1);
        assertThat(crypto.decrypt(cifrado1)).isEqualTo(original);
        assertThat(crypto.decrypt(cifrado2)).isEqualTo(original);
    }

    @Test
    void encryptYDecrypt_cuandoNull_retornanNull() {
        assertThat(crypto.encrypt(null)).isNull();
        assertThat(crypto.decrypt(null)).isNull();
    }
}
