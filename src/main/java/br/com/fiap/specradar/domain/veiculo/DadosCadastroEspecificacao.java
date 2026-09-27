package br.com.fiap.specradar.domain.veiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroEspecificacao(
        @NotBlank
        @Size(max = 60)
        String atributo,

        @NotBlank
        @Size(max = 255)
        String valor,

        @Size(max = 255)
        String fonte) {
}
