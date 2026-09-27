package br.com.fiap.specradar.infra.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class Metricas {
    private final MeterRegistry registry;

    public Metricas(MeterRegistry registry) {
        this.registry = registry;
    }

    public void incrementarLoginFalhou(String ip) {
        Counter.builder("specradar_auth_login_failure_total")
                .tag("ip", ip)
                .register(registry)
                .increment();
    }

    public void incrementarRateLimitExcedido(String rota) {
        Counter.builder("specradar_rate_limit_exceeded_total")
                .tag("rota", rota)
                .register(registry)
                .increment();
    }

    public void incrementarTrocaDePerfil() {
        Counter.builder("specradar_role_changes_total")
                .register(registry)
                .increment();
    }

    public void incrementarErroFonte() {
        Counter.builder("specradar_source_fetch_errors_total")
                .register(registry)
                .increment();
    }
}
