package br.com.fiap.specradar.service;

import br.com.fiap.specradar.domain.atributo.Atributo;
import br.com.fiap.specradar.domain.atributo.AtributoRepository;
import br.com.fiap.specradar.domain.pesquisa.*;
import br.com.fiap.specradar.domain.pesquisa.FichaTecnica.EspecificacaoResultado;
import br.com.fiap.specradar.domain.pesquisa.FichaTecnica.VeiculoResumo;
import br.com.fiap.specradar.domain.usuario.Usuario;
import br.com.fiap.specradar.domain.veiculo.EspecificacaoVeiculo;
import br.com.fiap.specradar.domain.veiculo.Veiculo;
import br.com.fiap.specradar.domain.veiculo.VeiculoRepository;
import br.com.fiap.specradar.infra.Sanitizador;
import br.com.fiap.specradar.infra.ml.MLClient;
import br.com.fiap.specradar.infra.ml.MLPredictResponse;
import br.com.fiap.specradar.infra.ml.MLPredictResponse.MLResultado;
import br.com.fiap.specradar.infra.observability.EventoLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PesquisaService {
    private static final String NAO_DISPONIVEL = "Não disponível";

    private final PesquisaRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final AtributoRepository atributoRepository;
    private final MLClient mlClient;
    private final EventoLogger eventoLogger;

    @Transactional
    public Pesquisa pesquisar(Usuario usuario, DadosCadastroPesquisa dadosOriginais) {
        DadosCadastroPesquisa dados = sanitizar(dadosOriginais);

        Optional<Veiculo> veiculo = veiculoRepository
                .findByMarcaIgnoreCaseAndModeloIgnoreCaseAndVersaoIgnoreCaseAndAtivoTrue(
                        dados.marca(), dados.modelo(), dados.versao());

        Optional<MLPredictResponse> respostaML = mlClient.prever(dados.atributos());

        List<EspecificacaoResultado> especificacoes = respostaML.isPresent()
                ? mapearComML(dados.atributos(), respostaML.get(), veiculo)
                : mapearComFallback(dados.atributos(), veiculo);

        FichaTecnica ficha = new FichaTecnica(
                new VeiculoResumo(dados.marca(), dados.modelo(), dados.versao()),
                especificacoes,
                LocalDateTime.now());

        Pesquisa pesquisa = repository.save(new Pesquisa(usuario, dados, ficha));

        eventoLogger.registrar("SPEC_SEARCH_EXECUTED", Map.of(
                "login", eventoLogger.mascararLogin(usuario.getLogin()),
                "marca", dados.marca(),
                "modelo", dados.modelo(),
                "versao", dados.versao(),
                "quantidadeAtributos", String.valueOf(dados.atributos().size())));

        return pesquisa;
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemPesquisa> listarPesquisas(Usuario usuario, Pageable pageable) {
        return repository.findAllByUsuario(usuario, pageable).map(DadosListagemPesquisa::new);
    }

    @Transactional(readOnly = true)
    public FichaTecnica detalharPesquisa(Usuario usuario, Long id) {
        Pesquisa pesquisa = repository.findById(id)
                .filter(encontrada -> encontrada.pertenceA(usuario))
                .orElseThrow(() -> new PesquisaNotFoundException("ID da pesquisa informado não existe"));
        return pesquisa.getResultado();
    }

    private List<EspecificacaoResultado> mapearComML(
            List<String> atributosSolicitados, MLPredictResponse resposta, Optional<Veiculo> veiculo) {
        return atributosSolicitados.stream()
                .map(solicitado -> {
                    MLResultado resultado = resposta.resultados().stream()
                            .filter(r -> r.entrada().equals(solicitado))
                            .findFirst()
                            .orElse(null);
                    if (!MLClient.confiavel(resultado)) {
                        Double confianca = resultado != null ? resultado.score() : null;
                        return naoDisponivel(solicitado, confianca);
                    }
                    return resolverComCatalogo(solicitado, resultado.atributo(), resultado.score(), veiculo);
                })
                .toList();
    }

    private List<EspecificacaoResultado> mapearComFallback(List<String> atributosSolicitados, Optional<Veiculo> veiculo) {
        return atributosSolicitados.stream()
                .map(solicitado -> {
                    String normalizado = solicitado.trim();
                    Optional<Atributo> encontrado = atributoRepository.findAll().stream()
                            .filter(Atributo::isAtivo)
                            .filter(atributo -> atributo.getCodigo().equalsIgnoreCase(normalizado)
                                    || atributo.getNome().equalsIgnoreCase(normalizado))
                            .findFirst();
                    return encontrado
                            .map(atributo -> resolverComCatalogo(solicitado, atributo.getCodigo(), 1.0, veiculo))
                            .orElseGet(() -> naoDisponivel(solicitado, null));
                })
                .toList();
    }

    private EspecificacaoResultado resolverComCatalogo(
            String solicitado, String codigoAtributo, Double confianca, Optional<Veiculo> veiculo) {
        Optional<Atributo> atributo = atributoRepository.findByCodigoAndAtivoTrue(codigoAtributo);
        if (atributo.isEmpty()) {
            return naoDisponivel(solicitado, confianca);
        }

        Optional<String> valor = veiculo.flatMap(v -> v.getEspecificacoes().stream()
                .filter(especificacao -> especificacao.getAtributo().getId().equals(atributo.get().getId()))
                .map(EspecificacaoVeiculo::getValor)
                .findFirst());

        return new EspecificacaoResultado(
                solicitado,
                atributo.get().getNome(),
                valor.orElse(NAO_DISPONIVEL),
                atributo.get().getUnidade() != null ? atributo.get().getUnidade() : NAO_DISPONIVEL,
                valor.isPresent() ? StatusEspecificacao.ENCONTRADO : StatusEspecificacao.NAO_DISPONIVEL,
                confianca);
    }

    private EspecificacaoResultado naoDisponivel(String solicitado, Double confianca) {
        return new EspecificacaoResultado(solicitado, NAO_DISPONIVEL, NAO_DISPONIVEL, NAO_DISPONIVEL,
                StatusEspecificacao.NAO_DISPONIVEL, confianca);
    }

    private DadosCadastroPesquisa sanitizar(DadosCadastroPesquisa dados) {
        return new DadosCadastroPesquisa(
                Sanitizador.sanitizarTextoLivre(dados.marca()),
                Sanitizador.sanitizarTextoLivre(dados.modelo()),
                Sanitizador.sanitizarTextoLivre(dados.versao()),
                dados.atributos().stream().map(Sanitizador::sanitizarTextoLivre).toList());
    }
}
