package br.com.fiap.specradar.domain.atributo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroAtributo(
        @NotBlank
        @Size(max = 60)
        String codigo,

        @NotBlank
        @Size(max = 150)
        String nome,

        @Size(max = 30)
        String unidade,

        @NotBlank
        @Size(max = 60)
        String categoria) {
}
