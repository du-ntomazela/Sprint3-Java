package br.com.fiap.specradar.domain.veiculo;

import jakarta.validation.constraints.Size;

public record DadosAtualizacaoVeiculo(
        @Size(max = 80)
        String marca,

        @Size(max = 80)
        String modelo,

        @Size(max = 80)
        String versao,

        Integer ano) {
}
