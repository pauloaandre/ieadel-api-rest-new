package ex.model;

public record UsuarioResponseDTO(Long id, String nome, String email, Perfil perfil, Long idCongregacao, String nomeCongregacao) {
    public static UsuarioResponseDTO fromUsuario(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil(),
            usuario.getCongregacao() != null ? usuario.getCongregacao().getIdCongregacao() : null,
            usuario.getCongregacao() != null ? usuario.getCongregacao().getNome() : null
        );
    }
}
