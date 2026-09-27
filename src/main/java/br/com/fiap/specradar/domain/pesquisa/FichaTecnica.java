package br.com.fiap.specradar.domain.pesquisa;

import java.time.LocalDateTime;
import java.util.List;

public record FichaTecnica(
        VeiculoResumo veiculo,
        List<EspecificacaoResultado> especificacoes,
        LocalDateTime geradoEm) {

    public record VeiculoResumo(String marca, String modelo, String versao) {
    }

    public record EspecificacaoResultado(
            String atributoSolicitado,
            String atributoCanonico,
            String valor,
            String unidade,
            StatusEspecificacao status,
            Double confianca) {
    }
}
