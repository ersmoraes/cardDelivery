package com.banking.carddelivery.provider;

import com.banking.carddelivery.domain.dto.EnderecoDTO;
import com.banking.carddelivery.exception.CepNaoEncontradoException;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class ViaCepProviderTest {

    private ViaCepProvider provider;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmInfo) {
        provider = new ViaCepProvider(WebClient.builder(), wmInfo.getHttpBaseUrl());
    }

    @Test
    void buscarEndereco_validCep_returnsEnderecoDTO() {
        stubFor(get(urlEqualTo("/ws/01310100/json/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "logradouro": "Avenida Paulista",
                                  "bairro": "Bela Vista",
                                  "localidade": "São Paulo",
                                  "uf": "SP"
                                }
                                """)));

        EnderecoDTO result = provider.buscarEndereco("01310100");

        assertThat(result.getLogradouro()).isEqualTo("Avenida Paulista");
        assertThat(result.getBairro()).isEqualTo("Bela Vista");
        assertThat(result.getCidade()).isEqualTo("São Paulo");
        assertThat(result.getUf()).isEqualTo("SP");
    }

    @Test
    void buscarEndereco_erroTrue_throwsCepNaoEncontrado() {
        stubFor(get(urlEqualTo("/ws/99999999/json/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"erro\": true}")));

        assertThatThrownBy(() -> provider.buscarEndereco("99999999"))
                .isInstanceOf(CepNaoEncontradoException.class)
                .hasMessageContaining("99999999");
    }

    @Test
    void buscarEndereco_allFieldsNull_returnsPartialDto() {
        stubFor(get(urlEqualTo("/ws/00000000/json/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"uf\": \"RJ\"}")));

        EnderecoDTO result = provider.buscarEndereco("00000000");

        assertThat(result.getUf()).isEqualTo("RJ");
        assertThat(result.getLogradouro()).isNull();
    }
}
