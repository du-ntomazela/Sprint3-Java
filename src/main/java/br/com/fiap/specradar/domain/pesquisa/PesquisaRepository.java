package br.com.fiap.specradar.domain.pesquisa;

import br.com.fiap.specradar.domain.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesquisaRepository extends JpaRepository<Pesquisa, Long> {
    Page<Pesquisa> findAllByUsuario(Usuario usuario, Pageable pageable);
}
