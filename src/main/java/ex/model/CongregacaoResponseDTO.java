package ex.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CongregacaoResponseDTO(Long idCongregacao, String nome, String endereco, Integer quantidadeMembros) {
    public static CongregacaoResponseDTO fromCongregacao(Congregacao congregacao, boolean includeMembers) {
        Integer membrosContagem = null;
        if (includeMembers && congregacao.getUsuarios() != null) {
            membrosContagem = (int) congregacao.getUsuarios().stream()
                .filter(u -> u.getAtivo() != null && u.getAtivo())
                .filter(u -> u.getNome() != null && !u.getNome().startsWith("Visitante"))
                .count();
        }

        return new CongregacaoResponseDTO(
            congregacao.getIdCongregacao(),
            congregacao.getNome(),
            congregacao.getEndereco(),
            membrosContagem
        );
    }
}
