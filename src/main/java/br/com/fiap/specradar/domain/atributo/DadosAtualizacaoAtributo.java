package br.com.fiap.specradar.domain.atributo;

import jakarta.validation.constraints.Size;

public record DadosAtualizacaoAtributo(
        @Size(max = 150)
        String nome,

        @Size(max = 30)
        String unidade,

        @Size(max = 60)
        String categoria) {
}
