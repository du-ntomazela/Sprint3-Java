package br.com.fiap.specradar.domain.veiculo;

import br.com.fiap.specradar.domain.atributo.Atributo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "EspecificacaoVeiculo")
@Table(name = "especificacoes_veiculo")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class EspecificacaoVeiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne
    @JoinColumn(name = "atributo_id")
    private Atributo atributo;

    private String valor;
    private String fonte;

    public EspecificacaoVeiculo(Veiculo veiculo, Atributo atributo, String valor, String fonte) {
        this.veiculo = veiculo;
        this.atributo = atributo;
        this.valor = valor;
        this.fonte = fonte;
    }

    public void atualizarInformacoes(String valor, String fonte) {
        if (valor != null && !valor.isBlank()) {
            this.valor = valor;
        }
        if (fonte != null && !fonte.isBlank()) {
            this.fonte = fonte;
        }
    }
}
