package ex.controller;

import ex.model.*;
import ex.model.repository.CongregacaoRepository;
import ex.model.repository.UsuarioRepository;
import ex.service.MovimentacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CongregacaoRepository congregacaoRepository;

    @Autowired
    private MovimentacaoService movimentacaoService;

    // --- GESTÃO DE USUÁRIOS ---

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodosUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioRepository.findAll()
                .stream()
                .filter(usuario -> usuario.getAtivo() != null && usuario.getAtivo() && usuario.getPerfil() != Perfil.SUPER_ADMIN)
                .map(UsuarioResponseDTO::fromUsuario)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @PatchMapping("/usuarios/{id}/perfil")
    public ResponseEntity<?> mudarPerfil(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String novoPerfil = body.get("perfil");
            return usuarioRepository.findById(id)
                    .map(usuario -> {
                        usuario.setPerfil(Perfil.valueOf(novoPerfil.toUpperCase()));
                        usuarioRepository.save(usuario);
                        return ResponseEntity.ok(UsuarioResponseDTO.fromUsuario(usuario));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Perfil inválido ou erro ao atualizar.");
        }
    }

    @PatchMapping("/usuarios/{id}/congregacao")
    public ResponseEntity<?> mudarCongregacao(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            Long idCongregacao = body.get("idCongregacao");
            return usuarioRepository.findById(id)
                    .map(usuario -> {
                        Congregacao congregacao = congregacaoRepository.findById(idCongregacao)
                                .orElseThrow(() -> new IllegalArgumentException("Congregação não encontrada"));
                        usuario.setCongregacao(congregacao);
                        usuarioRepository.save(usuario);
                        return ResponseEntity.ok(UsuarioResponseDTO.fromUsuario(usuario));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return ResponseEntity.ok(Map.of("message", "Usuário deletado com sucesso pelo Super Admin."));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- VISUALIZAÇÃO DE DADOS (APENAS LEITURA) ---

    @GetMapping("/movimentacoes")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarMovimentacoes(
            @RequestParam TipoMovimentacao tipo,
            @RequestParam String mes,
            @RequestParam String ano,
            @RequestParam(required = false) Integer idCongregacao) {
        
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorMesAnoECongregacao(tipo, mes, ano, idCongregacao);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/movimentacoes/totais")
    public ResponseEntity<Map<String, BigDecimal>> getTotais(
            @RequestParam String mes,
            @RequestParam String ano,
            @RequestParam(required = false) Integer idCongregacao) {

        Map<String, BigDecimal> totais = Map.of(
            "dizimo", movimentacaoService.calcularTotalECongregacao(TipoMovimentacao.DIZIMO, mes, ano, idCongregacao),
            "oferta", movimentacaoService.calcularTotalECongregacao(TipoMovimentacao.OFERTA, mes, ano, idCongregacao),
            "despesa", movimentacaoService.calcularTotalECongregacao(TipoMovimentacao.DESPESA, mes, ano, idCongregacao)
        );

        return ResponseEntity.ok(totais);
    }

    @GetMapping("/movimentacoes/totalGeral")
    public ResponseEntity<Map<String, BigDecimal>> getTotalGeral(@RequestParam(required = false) Integer idCongregacao) {
        return ResponseEntity.ok(Map.of("total", movimentacaoService.calcularTotalGeralPorCongregacao(idCongregacao)));
    }
}
