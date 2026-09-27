package br.com.fiap.specradar.domain.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticacaoService implements UserDetailsService {
    private final UsuarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDetails usuario = repository.findByLoginAndAtivoTrue(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Login ou senha inválidos");
        }
        return usuario;
    }
}
