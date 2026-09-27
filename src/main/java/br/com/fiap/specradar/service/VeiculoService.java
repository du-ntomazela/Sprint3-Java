package br.com.fiap.specradar.service;

import br.com.fiap.specradar.domain.atributo.Atributo;
import br.com.fiap.specradar.domain.atributo.AtributoRepository;
import br.com.fiap.specradar.domain.veiculo.*;
import br.com.fiap.specradar.infra.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VeiculoService {
    private final VeiculoRepository repository;
    private final AtributoRepository atributoRepository;

    @Transactional
    public DadosDetalhamentoVeiculo cadastrarVeiculo(DadosCadastroVeiculo dados) {
        Veiculo veiculo = new Veiculo(dados);
        repository.save(veiculo);
        adicionarEspecificacoes(veiculo, dados.especificacoes());
        return new DadosDetalhamentoVeiculo(veiculo);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemVeiculo> listarVeiculos(Pageable pageable) {
        return repository.findAllByAtivoTrue(pageable).map(DadosListagemVeiculo::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoVeiculo detalharVeiculo(Long id) {
        return new DadosDetalhamentoVeiculo(buscarAtivo(id));
    }

    @Transactional
    public DadosDetalhamentoVeiculo atualizarVeiculo(Long id, DadosAtualizacaoVeiculo dados) {
        Veiculo veiculo = buscarAtivo(id);
        veiculo.atualizarInformacoes(dados);
        return new DadosDetalhamentoVeiculo(veiculo);
    }

    @Transactional
    public void excluirVeiculo(Long id) {
        buscarAtivo(id).excluir();
    }

    private void adicionarEspecificacoes(Veiculo veiculo, java.util.List<DadosCadastroEspecificacao> especificacoes) {
        if (especificacoes == null) {
            return;
        }
        especificacoes.forEach(dados -> {
            Atributo atributo = atributoRepository.findByCodigoAndAtivoTrue(dados.atributo())
                    .orElseThrow(() -> new RegraDeNegocioException(
                            "O atributo '" + dados.atributo() + "' não existe no catálogo canônico"));
            veiculo.getEspecificacoes().add(new EspecificacaoVeiculo(veiculo, atributo, dados.valor(), dados.fonte()));
        });
    }

    private Veiculo buscarAtivo(Long id) {
        return repository.findById(id)
                .filter(Veiculo::isAtivo)
                .orElseThrow(() -> new VeiculoNotFoundException("ID do veículo informado não existe"));
    }
}
