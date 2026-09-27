package br.com.fiap.specradar.domain.pesquisa.converter;

import br.com.fiap.specradar.domain.pesquisa.FichaTecnica;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FichaTecnicaConverter implements AttributeConverter<FichaTecnica, String> {
    @Override
    public String convertToDatabaseColumn(FichaTecnica ficha) {
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(ficha);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao serializar ficha técnica", e);
        }
    }

    @Override
    public FichaTecnica convertToEntityAttribute(String dbData) {
        try {
            return JsonMapperHolder.MAPPER.readValue(dbData, FichaTecnica.class);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao desserializar ficha técnica", e);
        }
    }
}
