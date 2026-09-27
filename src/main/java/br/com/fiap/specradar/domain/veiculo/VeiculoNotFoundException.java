package br.com.fiap.specradar.domain.veiculo;

import br.com.fiap.specradar.infra.exception.RecursoNaoEncontradoException;

public class VeiculoNotFoundException extends RecursoNaoEncontradoException {
    public VeiculoNotFoundException(String message) {
        super(message);
    }
}
