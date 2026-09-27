package br.com.fiap.specradar.domain.pesquisa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DadosCadastroPesquisa(
        @NotBlank
        @Size(max = 80)
        String marca,

        @NotBlank
        @Size(max = 80)
        String modelo,

        @NotBlank
        @Size(max = 80)
        String versao,

        @NotEmpty
        @Size(min = 1, max = 50)
        List<@NotBlank @Size(max = 60) String> atributos) {
}
