package ex.service;

import ex.model.Perfil;
import ex.model.Usuario;
import ex.model.UsuarioResponseDTO;
import ex.model.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<UsuarioResponseDTO> buscarUsuariosPorNomeNaCongregacao(String nome) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long idCongregacao = usuarioLogado.getCongregacao() != null ? usuarioLogado.getCongregacao().getIdCongregacao() : null;

        if (idCongregacao == null && usuarioLogado.getPerfil() == Perfil.SUPER_ADMIN) {
            // Se for SUPER_ADMIN sem congregação, talvez buscar em todas as congregações ou retornar vazio.
            // Para evitar erro, vamos buscar apenas por nome (precisa de novo método no repo ou ajustar este).
            // Por enquanto, vamos retornar vazio ou tratar no repo.
            // Vou assumir que SUPER_ADMIN sem congregação não deveria chamar este método específico "NaCongregacao" 
            // ou que ele deveria ver todos.
            return usuarioRepository.findByNomeContainingIgnoreCase(nome)
                    .stream()
                    .map(UsuarioResponseDTO::fromUsuario)
                    .toList();
        }

        return usuarioRepository.findByNomeContainingIgnoreCaseAndCongregacao(nome, idCongregacao)
                .stream()
                .map(UsuarioResponseDTO::fromUsuario)
                .toList();
    }
}
