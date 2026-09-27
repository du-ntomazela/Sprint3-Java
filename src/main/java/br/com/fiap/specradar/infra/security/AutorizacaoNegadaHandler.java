package br.com.fiap.specradar.infra.security;

import br.com.fiap.specradar.infra.exception.ErroPadrao;
import br.com.fiap.specradar.infra.observability.EventoLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class AutorizacaoNegadaHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    private final EventoLogger eventoLogger;

    public AutorizacaoNegadaHandler(ObjectMapper objectMapper, EventoLogger eventoLogger) {
        this.objectMapper = objectMapper;
        this.eventoLogger = eventoLogger;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        String login = autenticacao != null ? autenticacao.getName() : "anonimo";
        eventoLogger.registrar("ACCESS_DENIED", Map.of(
                "caminho", request.getRequestURI(),
                "login", eventoLogger.mascararLogin(login)));

        ErroPadrao erro = new ErroPadrao(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Você não tem permissão para acessar este recurso", request.getRequestURI());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}
