package br.com.fiap.specradar.domain.usuario;

import br.com.fiap.specradar.infra.exception.RecursoNaoEncontradoException;

public class UsuarioNotFoundException extends RecursoNaoEncontradoException {
    public UsuarioNotFoundException(String message) {
        super(message);
    }
}
