package br.com.fiap.specradar.infra.exception;

import br.com.fiap.specradar.infra.observability.EventoLogger;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalHandleException {
    private static final Logger logger = LoggerFactory.getLogger(GlobalHandleException.class);

    private final EventoLogger eventoLogger;

    @ExceptionHandler({RecursoNaoEncontradoException.class, EntityNotFoundException.class})
    public ResponseEntity<ErroPadrao> handleNaoEncontrado(RuntimeException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadrao> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErroPadrao.CampoErro> campos = ex.getFieldErrors().stream()
                .map(this::paraCampoErro)
                .toList();
        ErroPadrao erro = new ErroPadrao(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Um ou mais campos estão inválidos", request.getRequestURI(), campos);
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            HttpRequestMethodNotSupportedException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroPadrao> handleRequisicaoInvalida(Exception ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "Requisição inválida", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroPadrao> handleAutenticacao(AuthenticationException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Login ou senha inválidos", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroPadrao> handleAcessoNegado(AccessDeniedException ex, HttpServletRequest request) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        String login = autenticacao != null ? autenticacao.getName() : "anonimo";
        eventoLogger.registrar("ACCESS_DENIED", Map.of(
                "caminho", request.getRequestURI(),
                "login", eventoLogger.mascararLogin(login)));
        return construir(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso", request);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroPadrao> handleRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroPadrao> handleConflito(DataIntegrityViolationException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, "O registro conflita com um recurso já existente", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadrao> handleGenerico(Exception ex, HttpServletRequest request) {
        logger.error("Erro não tratado na requisição {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", request);
    }

    private ResponseEntity<ErroPadrao> construir(HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(status.value(), status.getReasonPhrase(), mensagem, request.getRequestURI());
        return ResponseEntity.status(status).body(erro);
    }

    private ErroPadrao.CampoErro paraCampoErro(FieldError erro) {
        return new ErroPadrao.CampoErro(erro.getField(), erro.getDefaultMessage());
    }
}
