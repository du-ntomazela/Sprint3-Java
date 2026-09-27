package br.com.fiap.specradar.domain.pesquisa;

import br.com.fiap.specradar.domain.pesquisa.converter.FichaTecnicaConverter;
import br.com.fiap.specradar.domain.pesquisa.converter.ListaStringConverter;
import br.com.fiap.specradar.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "Pesquisa")
@Table(name = "pesquisas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Pesquisa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String marca;
    private String modelo;
    private String versao;

    @Convert(converter = ListaStringConverter.class)
    @Column(columnDefinition = "json")
    private List<String> atributosSolicitados;

    @Convert(converter = FichaTecnicaConverter.class)
    @Column(columnDefinition = "json")
    private FichaTecnica resultado;

    private LocalDateTime criadaEm;

    public Pesquisa(Usuario usuario, DadosCadastroPesquisa dados, FichaTecnica resultado) {
        this.usuario = usuario;
        this.marca = dados.marca();
        this.modelo = dados.modelo();
        this.versao = dados.versao();
        this.atributosSolicitados = dados.atributos();
        this.resultado = resultado;
        this.criadaEm = LocalDateTime.now();
    }

    public boolean pertenceA(Usuario usuario) {
        return this.usuario.getId().equals(usuario.getId());
    }
}
