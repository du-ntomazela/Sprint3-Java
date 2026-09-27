package br.com.fiap.specradar.domain.pesquisa;

import br.com.fiap.specradar.infra.exception.RecursoNaoEncontradoException;

public class PesquisaNotFoundException extends RecursoNaoEncontradoException {
    public PesquisaNotFoundException(String message) {
        super(message);
    }
}
