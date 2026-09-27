package br.com.fiap.specradar.domain.veiculo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "Veiculo")
@Table(name = "veiculos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String marca;
    private String modelo;
    private String versao;
    private Integer ano;
    private boolean ativo = true;

    @OneToMany(mappedBy = "veiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EspecificacaoVeiculo> especificacoes = new ArrayList<>();

    public Veiculo(DadosCadastroVeiculo dados) {
        this.marca = dados.marca();
        this.modelo = dados.modelo();
        this.versao = dados.versao();
        this.ano = dados.ano();
    }

    public void atualizarInformacoes(DadosAtualizacaoVeiculo dados) {
        if (dados.marca() != null && !dados.marca().isBlank()) {
            this.marca = dados.marca();
        }
        if (dados.modelo() != null && !dados.modelo().isBlank()) {
            this.modelo = dados.modelo();
        }
        if (dados.versao() != null && !dados.versao().isBlank()) {
            this.versao = dados.versao();
        }
        if (dados.ano() != null) {
            this.ano = dados.ano();
        }
    }

    public void excluir() {
        this.ativo = false;
    }
}
