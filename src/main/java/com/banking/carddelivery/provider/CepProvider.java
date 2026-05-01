package com.banking.carddelivery.provider;

import com.banking.carddelivery.domain.dto.EnderecoDTO;

public interface CepProvider {
    EnderecoDTO buscarEndereco(String cep);
}
