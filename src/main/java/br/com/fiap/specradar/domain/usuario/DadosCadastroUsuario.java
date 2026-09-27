package br.com.fiap.specradar.domain.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
        @NotBlank
        String nome,

        @NotBlank
        @Size(max = 150)
        String login,

        @NotBlank
        @Size(min = 8, max = 100)
        String senha,

        @NotNull
        Perfil perfil) {
}
