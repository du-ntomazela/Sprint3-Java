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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final int TENTATIVAS_POR_MINUTO = 5;
    private static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Instant> bloqueadosAte = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final EventoLogger eventoLogger;
    private final Metricas metricas;

    public LoginRateLimitFilter(ObjectMapper objectMapper, EventoLogger eventoLogger, Metricas metricas) {
        this.objectMapper = objectMapper;
        this.eventoLogger = eventoLogger;
        this.metricas = metricas;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ip = recuperarIp(request);

        Instant bloqueioAtivo = bloqueadosAte.get(ip);
        if (bloqueioAtivo != null) {
            if (Instant.now().isBefore(bloqueioAtivo)) {
                responder429(response, request);
                return;
            }
            bloqueadosAte.remove(ip);
            buckets.remove(ip);
        }

        Bucket bucket = buckets.computeIfAbsent(ip, this::novoBucket);
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        bloqueadosAte.put(ip, Instant.now().plus(DURACAO_BLOQUEIO));
        responder429(response, request);
    }

    private Bucket novoBucket(String ip) {
        Bandwidth limite = Bandwidth.builder()
                .capacity(TENTATIVAS_POR_MINUTO)
                .refillIntervally(TENTATIVAS_POR_MINUTO, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limite).build();
    }

    private void responder429(HttpServletResponse response, HttpServletRequest request) throws IOException {
        eventoLogger.registrar("RATE_LIMIT_EXCEEDED", Map.of("caminho", request.getRequestURI()));
        metricas.incrementarRateLimitExcedido("login");

        ErroPadrao erro = new ErroPadrao(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Muitas tentativas de login. Tente novamente em alguns minutos", request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }

    private String recuperarIp(HttpServletRequest request) {
        String encaminhadoPor = request.getHeader("X-Forwarded-For");
        if (encaminhadoPor != null && !encaminhadoPor.isBlank()) {
            return encaminhadoPor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
