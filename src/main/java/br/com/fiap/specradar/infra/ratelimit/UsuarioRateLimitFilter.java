package br.com.fiap.specradar.infra.ratelimit;

import br.com.fiap.specradar.infra.exception.ErroPadrao;
import br.com.fiap.specradar.infra.observability.EventoLogger;
import br.com.fiap.specradar.infra.observability.Metricas;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UsuarioRateLimitFilter extends OncePerRequestFilter {
    private static final int REQUISICOES_POR_MINUTO = 60;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final EventoLogger eventoLogger;
    private final Metricas metricas;

    public UsuarioRateLimitFilter(ObjectMapper objectMapper, EventoLogger eventoLogger, Metricas metricas) {
        this.objectMapper = objectMapper;
        this.eventoLogger = eventoLogger;
        this.metricas = metricas;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(autenticacao.getName(), login -> novoBucket());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        eventoLogger.registrar("RATE_LIMIT_EXCEEDED", Map.of(
                "caminho", request.getRequestURI(),
                "login", eventoLogger.mascararLogin(autenticacao.getName())));
        metricas.incrementarRateLimitExcedido("geral");

        ErroPadrao erro = new ErroPadrao(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Limite de requisições excedido. Tente novamente em instantes", request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }

    private Bucket novoBucket() {
        Bandwidth limite = Bandwidth.builder()
                .capacity(REQUISICOES_POR_MINUTO)
                .refillIntervally(REQUISICOES_POR_MINUTO, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limite).build();
    }
}
