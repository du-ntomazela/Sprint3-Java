package br.com.fiap.specradar.controller;

import br.com.fiap.specradar.domain.veiculo.DadosAtualizacaoVeiculo;
import br.com.fiap.specradar.domain.veiculo.DadosCadastroVeiculo;
import br.com.fiap.specradar.domain.veiculo.DadosDetalhamentoVeiculo;
import br.com.fiap.specradar.domain.veiculo.DadosListagemVeiculo;
import br.com.fiap.specradar.service.VeiculoService;
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
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Veículos e suas especificações técnicas")
public class VeiculoController {
    private final VeiculoService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um veículo com suas especificações técnicas")
    public ResponseEntity<DadosDetalhamentoVeiculo> cadastrarVeiculo(
            @RequestBody @Valid DadosCadastroVeiculo dados, UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoVeiculo dto = service.cadastrarVeiculo(dados);
        URI uri = uriBuilder.path("/veiculos/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Lista os veículos ativos, de forma paginada")
    public ResponseEntity<Page<DadosListagemVeiculo>> listarVeiculos(
            @ParameterObject @PageableDefault(size = 10, sort = "marca") Pageable pageable) {
        return ResponseEntity.ok(service.listarVeiculos(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Detalha um veículo e suas especificações")
    public ResponseEntity<DadosDetalhamentoVeiculo> detalharVeiculo(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharVeiculo(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza os dados cadastrais de um veículo")
    public ResponseEntity<DadosDetalhamentoVeiculo> atualizarVeiculo(
            @PathVariable Long id, @RequestBody @Valid DadosAtualizacaoVeiculo dados) {
        return ResponseEntity.ok(service.atualizarVeiculo(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui (logicamente) um veículo")
    public ResponseEntity<Void> excluirVeiculo(@PathVariable Long id) {
        service.excluirVeiculo(id);
        return ResponseEntity.noContent().build();
    }
}
