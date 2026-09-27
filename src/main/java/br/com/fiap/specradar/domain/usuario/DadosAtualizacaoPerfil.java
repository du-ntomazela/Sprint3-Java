package br.com.fiap.specradar.domain.usuario;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoPerfil(
        @NotNull
        Perfil perfil) {
}
