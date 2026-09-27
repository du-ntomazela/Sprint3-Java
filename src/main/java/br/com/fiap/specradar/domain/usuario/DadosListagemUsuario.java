package br.com.fiap.specradar.domain.usuario;

public record DadosListagemUsuario(
        Long id,
        String nome,
        String login,
        Perfil perfil) {
    public DadosListagemUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getLogin(), usuario.getPerfil());
    }
}
