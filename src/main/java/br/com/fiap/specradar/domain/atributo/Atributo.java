package br.com.fiap.specradar.domain.atributo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Atributo")
@Table(name = "atributos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Atributo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nome;
    private String unidade;
    private String categoria;
    private boolean ativo = true;

    public Atributo(DadosCadastroAtributo dados) {
        this.codigo = dados.codigo();
        this.nome = dados.nome();
        this.unidade = dados.unidade();
        this.categoria = dados.categoria();
    }

    public void atualizarInformacoes(DadosAtualizacaoAtributo dados) {
        if (dados.nome() != null && !dados.nome().isBlank()) {
            this.nome = dados.nome();
        }
        if (dados.unidade() != null && !dados.unidade().isBlank()) {
            this.unidade = dados.unidade();
        }
        if (dados.categoria() != null && !dados.categoria().isBlank()) {
            this.categoria = dados.categoria();
        }
    }

    public void excluir() {
        this.ativo = false;
    }
}
