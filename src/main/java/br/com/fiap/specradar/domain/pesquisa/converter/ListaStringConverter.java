package br.com.fiap.specradar.domain.pesquisa.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ListaStringConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> atributo) {
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(atributo);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao serializar lista de atributos", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return JsonMapperHolder.MAPPER.readValue(dbData, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao desserializar lista de atributos", e);
        }
    }
}
