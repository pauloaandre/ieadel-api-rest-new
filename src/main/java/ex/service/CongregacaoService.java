package ex.service;

import ex.model.Congregacao;
import ex.model.Perfil;
import ex.model.Usuario;
import ex.model.repository.CongregacaoRepository;
import ex.model.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CongregacaoService {

    @Autowired
    private CongregacaoRepository congregacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Congregacao criarCongregacao(Congregacao congregacao) {
        Congregacao novaCongregacao = congregacaoRepository.save(congregacao);
        
        criarUsuarioVisitante(novaCongregacao);
        
        return novaCongregacao;
    }

    private void criarUsuarioVisitante(Congregacao congregacao) {
        Usuario visitante = new Usuario();
        visitante.setNome("Visitante - " + congregacao.getNome());
        // Gerando um e-mail único baseado no ID ou UUID se o ID não for suficiente/seguro
        visitante.setEmail("visitante." + congregacao.getIdCongregacao() + "@ieadel.com");
        
        // Senha: Um hash aleatório e seguro
        String senhaAleatoria = UUID.randomUUID().toString();
        String senhaCriptografada = new BCryptPasswordEncoder().encode(senhaAleatoria);
        visitante.setSenha(senhaCriptografada);
        
        visitante.setPerfil(Perfil.USER);
        visitante.setAtivo(false);
        //visitante.setVerified(true);  Para evitar problemas com fluxo de verificação de e-mail se houver
        visitante.setCongregacao(congregacao);
        
        usuarioRepository.save(visitante);
    }
}
