package br.com.fiap.specradar.controller;

import br.com.fiap.specradar.domain.atributo.DadosAtualizacaoAtributo;
import br.com.fiap.specradar.domain.atributo.DadosCadastroAtributo;
import br.com.fiap.specradar.domain.atributo.DadosDetalhamentoAtributo;
import br.com.fiap.specradar.service.AtributoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/atributos")
@RequiredArgsConstructor
@Tag(name = "Atributos", description = "Catálogo canônico de atributos técnicos")
public class AtributoController {
    private final AtributoService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um novo atributo no catálogo canônico")
    public ResponseEntity<DadosDetalhamentoAtributo> cadastrarAtributo(
            @RequestBody @Valid DadosCadastroAtributo dados, UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoAtributo dto = service.cadastrarAtributo(dados);
        URI uri = uriBuilder.path("/atributos/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Lista os atributos ativos do catálogo, de forma paginada")
    public ResponseEntity<Page<DadosDetalhamentoAtributo>> listarAtributos(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listarAtributos(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Detalha um atributo pelo ID")
    public ResponseEntity<DadosDetalhamentoAtributo> detalharAtributo(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharAtributo(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um atributo do catálogo")
    public ResponseEntity<DadosDetalhamentoAtributo> atualizarAtributo(
            @PathVariable Long id, @RequestBody @Valid DadosAtualizacaoAtributo dados) {
        return ResponseEntity.ok(service.atualizarAtributo(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui (logicamente) um atributo do catálogo")
    public ResponseEntity<Void> excluirAtributo(@PathVariable Long id) {
        service.excluirAtributo(id);
        return ResponseEntity.noContent().build();
    }
}
