package br.com.fiap.specradar.domain.atributo;

public record DadosDetalhamentoAtributo(
        Long id,
        String codigo,
        String nome,
        String unidade,
        String categoria,
        boolean ativo) {
    public DadosDetalhamentoAtributo(Atributo atributo) {
        this(atributo.getId(), atributo.getCodigo(), atributo.getNome(), atributo.getUnidade(),
                atributo.getCategoria(), atributo.isAtivo());
    }
}
