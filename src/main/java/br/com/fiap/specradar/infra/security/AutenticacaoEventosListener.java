package br.com.fiap.specradar.infra.security;

import br.com.fiap.specradar.infra.observability.EventoLogger;
import br.com.fiap.specradar.infra.observability.Metricas;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AutenticacaoEventosListener {
    private final EventoLogger eventoLogger;
    private final Metricas metricas;

    @EventListener
    public void aoAutenticarComSucesso(AuthenticationSuccessEvent event) {
        String login = event.getAuthentication().getName();
        eventoLogger.registrar("AUTH_LOGIN_SUCCESS", Map.of("login", eventoLogger.mascararLogin(login)));
    }

    @EventListener
    public void aoFalharAutenticacao(AbstractAuthenticationFailureEvent event) {
        String login = String.valueOf(event.getAuthentication().getPrincipal());
        String ip = recuperarIp();
        eventoLogger.registrar("AUTH_LOGIN_FAILURE", Map.of(
                "login", eventoLogger.mascararLogin(login),
                "ip", ip));
        metricas.incrementarLoginFalhou(ip);
    }

    private String recuperarIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "desconhecido";
        }
        HttpServletRequest request = attributes.getRequest();
        String encaminhadoPor = request.getHeader("X-Forwarded-For");
        if (encaminhadoPor != null && !encaminhadoPor.isBlank()) {
            return encaminhadoPor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
