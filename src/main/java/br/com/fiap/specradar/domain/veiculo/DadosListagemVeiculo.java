package br.com.fiap.specradar.domain.veiculo;

public record DadosListagemVeiculo(
        Long id,
        String marca,
        String modelo,
        String versao,
        Integer ano) {
    public DadosListagemVeiculo(Veiculo veiculo) {
        this(veiculo.getId(), veiculo.getMarca(), veiculo.getModelo(), veiculo.getVersao(), veiculo.getAno());
    }
}
