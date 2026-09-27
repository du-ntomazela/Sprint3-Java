package br.com.fiap.specradar.controller;

import br.com.fiap.specradar.domain.usuario.DadosCadastroUsuario;
import br.com.fiap.specradar.domain.usuario.Perfil;
import br.com.fiap.specradar.domain.usuario.Usuario;
import br.com.fiap.specradar.domain.usuario.UsuarioRepository;
import br.com.fiap.specradar.infra.security.TokenService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestBase {
    static final String LOGIN_ADMIN = "admin@specradar.com";
    static final String SENHA_ADMIN = "admin12345";
    static final String LOGIN_ANALISTA = "analista@specradar.com";
    static final String SENHA_ANALISTA = "analista12345";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${api.security.token.secret}")
    private String secret;

    String tokenAdmin() {
        return tokenService.gerarToken(buscarUsuario(LOGIN_ADMIN));
    }

    String tokenAnalista() {
        return tokenService.gerarToken(buscarUsuario(LOGIN_ANALISTA));
    }

    Usuario buscarUsuario(String login) {
        return (Usuario) usuarioRepository.findByLoginAndAtivoTrue(login);
    }

    String tokenParaNovoUsuario(Perfil perfil) {
        String login = perfil.name().toLowerCase() + "-" + java.util.UUID.randomUUID() + "@specradar.com";
        DadosCadastroUsuario dados = new DadosCadastroUsuario("Usuário de teste", login, "senha12345", perfil);
        Usuario usuario = new Usuario(dados, "hash-nao-utilizado-neste-teste");
        usuarioRepository.save(usuario);
        return tokenService.gerarToken(usuario);
    }

    String tokenExpirado() {
        return JWT.create()
                .withIssuer("SpecRadar")
                .withSubject(LOGIN_ADMIN)
                .withClaim("perfil", "ADMIN")
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(secret));
    }

    String tokenAssinaturaInvalida() {
        return JWT.create()
                .withIssuer("SpecRadar")
                .withSubject(LOGIN_ADMIN)
                .withClaim("perfil", "ADMIN")
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256("outro-segredo-qualquer"));
    }
}
