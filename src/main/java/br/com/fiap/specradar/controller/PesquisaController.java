package br.com.fiap.specradar.controller;

import br.com.fiap.specradar.domain.pesquisa.DadosCadastroPesquisa;
import br.com.fiap.specradar.domain.pesquisa.DadosListagemPesquisa;
import br.com.fiap.specradar.domain.pesquisa.FichaTecnica;
import br.com.fiap.specradar.domain.pesquisa.Pesquisa;
import br.com.fiap.specradar.domain.usuario.Usuario;
import br.com.fiap.specradar.service.PesquisaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/pesquisas")
@RequiredArgsConstructor
@Tag(name = "Pesquisas", description = "Pesquisas de ficha técnica padronizada por veículo")
public class PesquisaController {
    private final PesquisaService service;

    @PostMapping
    @Operation(summary = "Executa uma pesquisa e retorna a ficha técnica padronizada do veículo")
    public ResponseEntity<FichaTecnica> pesquisar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody @Valid DadosCadastroPesquisa dados,
            UriComponentsBuilder uriBuilder) {
        Pesquisa pesquisa = service.pesquisar(usuario, dados);
        URI uri = uriBuilder.path("/pesquisas/{id}").buildAndExpand(pesquisa.getId()).toUri();
        return ResponseEntity.created(uri).body(pesquisa.getResultado());
    }

    @GetMapping
    @Operation(summary = "Lista as pesquisas realizadas pelo usuário autenticado")
    public ResponseEntity<Page<DadosListagemPesquisa>> listarPesquisas(
            @AuthenticationPrincipal Usuario usuario,
            @ParameterObject @PageableDefault(size = 10, sort = "criadaEm") Pageable pageable) {
        return ResponseEntity.ok(service.listarPesquisas(usuario, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma pesquisa do usuário autenticado (404 se pertencer a outro usuário)")
    public ResponseEntity<FichaTecnica> detalharPesquisa(
            @AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return ResponseEntity.ok(service.detalharPesquisa(usuario, id));
    }
}
