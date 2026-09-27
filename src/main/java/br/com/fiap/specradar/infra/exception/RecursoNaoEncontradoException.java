package br.com.fiap.specradar.infra.exception;

public abstract class RecursoNaoEncontradoException extends RuntimeException {
    protected RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
