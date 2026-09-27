package br.com.fiap.specradar.infra.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EventoLogger {
    private static final Logger logger = LoggerFactory.getLogger("SpecRadarEventos");

    public void registrar(String evento, Map<String, String> contexto) {
        try {
            MDC.put("evento", evento);
            contexto.forEach(MDC::put);
            logger.info(evento);
        } finally {
            MDC.remove("evento");
            contexto.keySet().forEach(MDC::remove);
        }
    }

    public String mascararLogin(String login) {
        if (login == null || login.isBlank()) {
            return "desconhecido";
        }
        int arroba = login.indexOf('@');
        String usuario = arroba > 0 ? login.substring(0, arroba) : login;
        String dominio = arroba > 0 ? login.substring(arroba) : "";
        String visivel = usuario.length() <= 2 ? usuario : usuario.substring(0, 2);
        return visivel + "***" + dominio;
    }
}
