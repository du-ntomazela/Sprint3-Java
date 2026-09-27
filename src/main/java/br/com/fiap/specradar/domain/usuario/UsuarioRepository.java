package br.com.fiap.specradar.domain.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    UserDetails findByLoginAndAtivoTrue(String login);

    boolean existsByLogin(String login);

    Page<Usuario> findAllByAtivoTrue(Pageable pageable);
}
