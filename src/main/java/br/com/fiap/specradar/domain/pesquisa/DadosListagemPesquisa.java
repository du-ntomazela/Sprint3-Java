package br.com.fiap.specradar.domain.pesquisa;

import java.time.LocalDateTime;

public record DadosListagemPesquisa(
        Long id,
        String marca,
        String modelo,
        String versao,
        LocalDateTime criadaEm) {
    public DadosListagemPesquisa(Pesquisa pesquisa) {
        this(pesquisa.getId(), pesquisa.getMarca(), pesquisa.getModelo(), pesquisa.getVersao(), pesquisa.getCriadaEm());
    }
}
