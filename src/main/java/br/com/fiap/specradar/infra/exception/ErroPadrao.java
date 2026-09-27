package br.com.fiap.specradar.infra.exception;

import java.time.Instant;
import java.util.List;

public record ErroPadrao(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoErro> campos) {

    public ErroPadrao(int status, String erro, String mensagem, String caminho) {
        this(Instant.now(), status, erro, mensagem, caminho, List.of());
    }

    public ErroPadrao(int status, String erro, String mensagem, String caminho, List<CampoErro> campos) {
        this(Instant.now(), status, erro, mensagem, caminho, campos);
    }

    public record CampoErro(String campo, String mensagem) {
    }
}
