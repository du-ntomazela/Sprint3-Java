package br.com.fiap.specradar.infra.seed;

import java.util.List;

public record VeiculoSeed(String marca, String modelo, String versao, Integer ano, List<EspecificacaoSeed> especificacoes) {

    public record EspecificacaoSeed(String atributo, String valor, String fonte) {
    }
}
