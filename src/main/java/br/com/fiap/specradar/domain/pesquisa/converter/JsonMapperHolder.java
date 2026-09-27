package br.com.fiap.specradar.domain.pesquisa.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

final class JsonMapperHolder {
    static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private JsonMapperHolder() {
    }
}
