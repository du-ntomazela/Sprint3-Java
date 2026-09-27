package br.com.fiap.specradar.domain.veiculo;

import java.util.List;

public record DadosDetalhamentoVeiculo(
        Long id,
        String marca,
        String modelo,
        String versao,
        Integer ano,
        boolean ativo,
        List<DadosDetalhamentoEspecificacao> especificacoes) {
    public DadosDetalhamentoVeiculo(Veiculo veiculo) {
        this(veiculo.getId(), veiculo.getMarca(), veiculo.getModelo(), veiculo.getVersao(), veiculo.getAno(),
                veiculo.isAtivo(), veiculo.getEspecificacoes().stream().map(DadosDetalhamentoEspecificacao::new).toList());
    }

    public record DadosDetalhamentoEspecificacao(
            String atributo,
            String valor,
            String fonte) {
        public DadosDetalhamentoEspecificacao(EspecificacaoVeiculo especificacao) {
            this(especificacao.getAtributo().getCodigo(), especificacao.getValor(), especificacao.getFonte());
        }
    }
}
