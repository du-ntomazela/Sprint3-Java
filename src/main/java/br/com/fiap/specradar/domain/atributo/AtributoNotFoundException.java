package br.com.fiap.specradar.domain.atributo;

import br.com.fiap.specradar.infra.exception.RecursoNaoEncontradoException;

public class AtributoNotFoundException extends RecursoNaoEncontradoException {
    public AtributoNotFoundException(String message) {
        super(message);
    }
}
