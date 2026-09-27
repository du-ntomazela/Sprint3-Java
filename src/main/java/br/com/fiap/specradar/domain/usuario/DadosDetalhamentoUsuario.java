package br.com.fiap.specradar.domain.usuario;

public record DadosDetalhamentoUsuario(
        Long id,
        String nome,
        String login,
        Perfil perfil,
        boolean ativo) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getLogin(), usuario.getPerfil(), usuario.isAtivo());
    }
}
