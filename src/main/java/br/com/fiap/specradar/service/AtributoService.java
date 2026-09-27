package br.com.fiap.specradar.service;

import br.com.fiap.specradar.domain.atributo.*;
import br.com.fiap.specradar.infra.observability.EventoLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AtributoService {
    private final AtributoRepository repository;
    private final EventoLogger eventoLogger;

    @Transactional
    public DadosDetalhamentoAtributo cadastrarAtributo(DadosCadastroAtributo dados) {
        Atributo atributo = new Atributo(dados);
        repository.save(atributo);
        registrarAlteracao("CRIADO", atributo.getCodigo());
        return new DadosDetalhamentoAtributo(atributo);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoAtributo> listarAtributos(Pageable pageable) {
        return repository.findAllByAtivoTrue(pageable).map(DadosDetalhamentoAtributo::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAtributo detalharAtributo(Long id) {
        return new DadosDetalhamentoAtributo(buscarAtivo(id));
    }

    @Transactional
    public DadosDetalhamentoAtributo atualizarAtributo(Long id, DadosAtualizacaoAtributo dados) {
        Atributo atributo = buscarAtivo(id);
        atributo.atualizarInformacoes(dados);
        registrarAlteracao("ATUALIZADO", atributo.getCodigo());
        return new DadosDetalhamentoAtributo(atributo);
    }

    @Transactional
    public void excluirAtributo(Long id) {
        Atributo atributo = buscarAtivo(id);
        atributo.excluir();
        registrarAlteracao("EXCLUIDO", atributo.getCodigo());
    }

    private Atributo buscarAtivo(Long id) {
        return repository.findById(id)
                .filter(Atributo::isAtivo)
                .orElseThrow(() -> new AtributoNotFoundException("ID do atributo informado não existe"));
    }

    private void registrarAlteracao(String acao, String codigo) {
        eventoLogger.registrar("ATTRIBUTE_CATALOG_CHANGED", Map.of("acao", acao, "codigo", codigo));
    }
}
