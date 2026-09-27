package br.com.fiap.specradar.infra.ml;

import java.util.List;

public record MLPredictResponse(List<MLResultado> resultados, String modeloVersao) {

    public record MLResultado(String entrada, String atributo, Double score) {
    }
}
