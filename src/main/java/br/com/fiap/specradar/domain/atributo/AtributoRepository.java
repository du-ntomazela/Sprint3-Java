package br.com.fiap.specradar.domain.atributo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtributoRepository extends JpaRepository<Atributo, Long> {
    Page<Atributo> findAllByAtivoTrue(Pageable pageable);

    boolean existsByCodigo(String codigo);

    Optional<Atributo> findByCodigoAndAtivoTrue(String codigo);

    List<Atributo> findAllByAtivoTrueAndCodigoIn(List<String> codigos);

    List<Atributo> findAllByAtivoTrueAndNomeIgnoreCaseIn(List<String> nomes);
}
