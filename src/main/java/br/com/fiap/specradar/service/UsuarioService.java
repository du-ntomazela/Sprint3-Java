package br.com.fiap.specradar.service;

import br.com.fiap.specradar.domain.usuario.*;
import br.com.fiap.specradar.infra.observability.EventoLogger;
import br.com.fiap.specradar.infra.observability.Metricas;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EventoLogger eventoLogger;
    private final Metricas metricas;

    @Transactional
    public DadosDetalhamentoUsuario cadastrarUsuario(DadosCadastroUsuario dados) {
        Usuario usuario = new Usuario(dados, passwordEncoder.encode(dados.senha()));
        repository.save(usuario);
        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemUsuario> listarUsuarios(Pageable pageable) {
        return repository.findAllByAtivoTrue(pageable).map(DadosListagemUsuario::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario detalharUsuario(Long id) {
        return new DadosDetalhamentoUsuario(buscarAtivo(id));
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario meuUsuario(UserDetails principal) {
        Usuario usuario = (Usuario) principal;
        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizarPerfil(Long id, DadosAtualizacaoPerfil dados) {
        Usuario usuario = buscarAtivo(id);
        usuario.atualizarPerfil(dados.perfil());
        eventoLogger.registrar("USER_ROLE_CHANGED", Map.of(
                "usuarioId", String.valueOf(id),
                "novoPerfil", dados.perfil().name()));
        metricas.incrementarTrocaDePerfil();
        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional
    public void excluirUsuario(Long id) {
        buscarAtivo(id).excluir();
    }

    private Usuario buscarAtivo(Long id) {
        return repository.findById(id)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new UsuarioNotFoundException("ID do usuário informado não existe"));
    }
}
