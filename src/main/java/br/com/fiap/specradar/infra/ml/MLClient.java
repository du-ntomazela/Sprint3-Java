package br.com.fiap.specradar.infra.ml;

import br.com.fiap.specradar.infra.observability.EventoLogger;
import br.com.fiap.specradar.infra.observability.Metricas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MLClient {
    private static final Logger logger = LoggerFactory.getLogger(MLClient.class);
    private static final double CONFIANCA_MINIMA = 0.6;
    private static final String ATRIBUTO_FORA_DO_CATALOGO = "fora_do_catalogo";

    private final RestClient restClient;
    private final EventoLogger eventoLogger;
    private final Metricas metricas;

    public MLClient(
            @Value("${specradar.ml.url}") String mlUrl,
            @Value("${specradar.ml.timeout-ms:5000}") long timeoutMs,
            EventoLogger eventoLogger,
            Metricas metricas) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder()
                .baseUrl(mlUrl)
                .requestFactory(factory)
                .build();
        this.eventoLogger = eventoLogger;
        this.metricas = metricas;
    }

    public Optional<MLPredictResponse> prever(List<String> atributos) {
        try {
            MLPredictResponse resposta = restClient.post()
                    .uri("/predict")
                    .body(new MLPredictRequest(atributos))
                    .retrieve()
                    .body(MLPredictResponse.class);
            if (!respostaValida(resposta, atributos)) {
                registrarFalha("Resposta do serviço de ML inválida ou incompleta");
                return Optional.empty();
            }
            return Optional.of(resposta);
        } catch (RestClientException exception) {
            registrarFalha(exception.getMessage());
            return Optional.empty();
        }
    }

    private boolean respostaValida(MLPredictResponse resposta, List<String> atributosSolicitados) {
        if (resposta == null || resposta.resultados() == null) {
            return false;
        }
        return resposta.resultados().stream().allMatch(resultado ->
                resultado.entrada() != null
                        && atributosSolicitados.contains(resultado.entrada())
                        && resultado.atributo() != null
                        && resultado.score() != null
                        && resultado.score() >= 0.0
                        && resultado.score() <= 1.0);
    }

    private void registrarFalha(String motivo) {
        logger.warn("Falha ao consultar o serviço de ML: {}", motivo);
        eventoLogger.registrar("SOURCE_FETCH_FAILED", Map.of("motivo", String.valueOf(motivo)));
        metricas.incrementarErroFonte();
    }

    public static boolean confiavel(MLPredictResponse.MLResultado resultado) {
        return resultado != null
                && resultado.score() != null
                && resultado.score() >= CONFIANCA_MINIMA
                && !ATRIBUTO_FORA_DO_CATALOGO.equals(resultado.atributo());
    }
}
