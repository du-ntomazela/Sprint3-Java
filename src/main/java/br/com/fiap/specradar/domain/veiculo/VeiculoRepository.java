package br.com.fiap.specradar.domain.veiculo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Page<Veiculo> findAllByAtivoTrue(Pageable pageable);

    Optional<Veiculo> findByMarcaIgnoreCaseAndModeloIgnoreCaseAndVersaoIgnoreCaseAndAtivoTrue(
            String marca, String modelo, String versao);
}
