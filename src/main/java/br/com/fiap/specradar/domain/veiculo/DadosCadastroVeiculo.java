package br.com.fiap.specradar.domain.veiculo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DadosCadastroVeiculo(
        @NotBlank
        @Size(max = 80)
        String marca,

        @NotBlank
        @Size(max = 80)
        String modelo,

        @NotBlank
        @Size(max = 80)
        String versao,

        @NotNull
        Integer ano,

        @Valid
        List<DadosCadastroEspecificacao> especificacoes) {
}
