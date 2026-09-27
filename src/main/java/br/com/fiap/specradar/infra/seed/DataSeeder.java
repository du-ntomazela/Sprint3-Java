package br.com.fiap.specradar.infra.seed;

import br.com.fiap.specradar.domain.atributo.Atributo;
import br.com.fiap.specradar.domain.atributo.AtributoRepository;
import br.com.fiap.specradar.domain.usuario.DadosCadastroUsuario;
import br.com.fiap.specradar.domain.usuario.Perfil;
import br.com.fiap.specradar.domain.usuario.Usuario;
import br.com.fiap.specradar.domain.usuario.UsuarioRepository;
import br.com.fiap.specradar.domain.veiculo.EspecificacaoVeiculo;
import br.com.fiap.specradar.domain.veiculo.Veiculo;
import br.com.fiap.specradar.domain.veiculo.VeiculoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final AtributoRepository atributoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VeiculoRepository veiculoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${specradar.seed.admin-login:admin@specradar.com}")
    private String adminLogin;

    @Value("${specradar.seed.admin-password}")
    private String adminSenha;

    @Value("${specradar.seed.analista-login:analista@specradar.com}")
    private String analistaLogin;

    @Value("${specradar.seed.analista-password}")
    private String analistaSenha;

    @Value("${specradar.seed.ranger-raptor-path:dados/ranger-raptor.json}")
    private String rangerRaptorPath;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        semearCatalogoDeAtributos();
        semearUsuarios();
        semearVeiculoDeValidacao();
    }

    private void semearCatalogoDeAtributos() throws Exception {
        if (atributoRepository.count() > 0) {
            return;
        }
        try (InputStream input = new ClassPathResource("dados/catalogo-atributos.json").getInputStream()) {
            List<CatalogoAtributoSeed> catalogo = objectMapper.readValue(input,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CatalogoAtributoSeed.class));
            catalogo.forEach(item -> atributoRepository.save(
                    new Atributo(null, item.codigo(), item.nome(), item.unidade(), item.categoria(), true)));
            logger.info("Catálogo de atributos semeado com {} itens", catalogo.size());
        }
    }

    private void semearUsuarios() {
        semearUsuario(adminLogin, adminSenha, Perfil.ADMIN, "Administrador SpecRadar");
        semearUsuario(analistaLogin, analistaSenha, Perfil.ANALISTA, "Analista SpecRadar");
    }

    private void semearUsuario(String login, String senha, Perfil perfil, String nome) {
        if (usuarioRepository.existsByLogin(login)) {
            return;
        }
        DadosCadastroUsuario dados = new DadosCadastroUsuario(nome, login, senha, perfil);
        usuarioRepository.save(new Usuario(dados, passwordEncoder.encode(senha)));
        logger.info("Usuário {} semeado com perfil {}", login, perfil);
    }

    private void semearVeiculoDeValidacao() {
        Path caminho = Path.of(rangerRaptorPath);
        if (!Files.exists(caminho)) {
            logger.warn("Arquivo de seed de veículo não encontrado em {}. Etapa ignorada.", caminho.toAbsolutePath());
            return;
        }
        try {
            VeiculoSeed seed = objectMapper.readValue(caminho.toFile(), VeiculoSeed.class);
            Optional<Veiculo> existente = veiculoRepository
                    .findByMarcaIgnoreCaseAndModeloIgnoreCaseAndVersaoIgnoreCaseAndAtivoTrue(
                            seed.marca(), seed.modelo(), seed.versao());
            if (existente.isPresent()) {
                return;
            }

            Veiculo veiculo = new Veiculo(null, seed.marca(), seed.modelo(), seed.versao(), seed.ano(), true, new ArrayList<>());
            veiculoRepository.save(veiculo);

            if (seed.especificacoes() != null) {
                seed.especificacoes().stream()
                        .filter(especificacao -> especificacao.valor() != null && !especificacao.valor().isBlank())
                        .forEach(especificacao -> atributoRepository.findByCodigoAndAtivoTrue(especificacao.atributo())
                                .ifPresent(atributo -> veiculo.getEspecificacoes().add(
                                        new EspecificacaoVeiculo(veiculo, atributo, especificacao.valor(), especificacao.fonte()))));
            }
            veiculoRepository.save(veiculo);
            logger.info("Veículo de validação {} {} {} semeado a partir de {}",
                    seed.marca(), seed.modelo(), seed.versao(), caminho);
        } catch (Exception e) {
            logger.error("Erro ao semear veículo de validação a partir de {}", caminho, e);
        }
    }
}
