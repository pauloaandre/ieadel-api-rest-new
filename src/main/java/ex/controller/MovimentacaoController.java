package ex.controller;

import ex.model.*;
import ex.model.repository.CongregacaoRepository;
import ex.model.repository.MovimentacaoRepository;
import ex.model.repository.UsuarioRepository;
import ex.service.MovimentacaoService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    @Autowired
    private MovimentacaoService movimentacaoService;

    // Rota para listar todas as movimentações (GET)
    @GetMapping
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorMesAno(
            @RequestParam TipoMovimentacao tipo,
            @RequestParam String mes,
            @RequestParam String ano,
            @RequestParam(required = false) Integer idCongregacao)  {

        Usuario logado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Integer congregacaoId = idCongregacao;
        
        if (congregacaoId == null) {
            if (logado.getCongregacao() == null) {
                if (logado.getPerfil() != Perfil.SUPER_ADMIN) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                congregacaoId = logado.getCongregacao().getIdCongregacao().intValue();
            }
        }

        // Validação dos parâmetros
        if (!mes.matches("\\d{2}") || !ano.matches("\\d{4}")) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorMesAnoECongregacao(tipo, mes, ano, congregacaoId);
        return ResponseEntity.ok(movimentacoes);
    }


    @GetMapping("/totais")
    public ResponseEntity<Map<String, BigDecimal>> getTotaisPorMes(
            @RequestParam String mes,
            @RequestParam String ano,
            @RequestParam(required = false) Integer idCongregacao) {

        Usuario logado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Integer congregacaoId = idCongregacao;
        
        if (congregacaoId == null) {
            if (logado.getCongregacao() == null) {
                if (logado.getPerfil() != Perfil.SUPER_ADMIN) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                congregacaoId = logado.getCongregacao().getIdCongregacao().intValue();
            }
        }

        Map<String, BigDecimal> totais = new HashMap<>();
        totais.put("dizimo", movimentacaoService.calcularTotalECongregacao(TipoMovimentacao.DIZIMO, mes, ano, congregacaoId));
        totais.put("oferta", movimentacaoService.calcularTotalECongregacao(TipoMovimentacao.OFERTA, mes, ano, congregacaoId));
        totais.put("despesa", movimentacaoService.calcularTotalECongregacao(TipoMovimentacao.DESPESA, mes, ano, congregacaoId));

        return ResponseEntity.ok(totais);
    }

    @GetMapping("/totalGeral")
    public ResponseEntity<Map<String, BigDecimal>> getTotalGeral(@RequestParam(required = false) Integer idCongregacao) {

        Usuario logado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Integer congregacaoId = idCongregacao;

        if (congregacaoId == null) {
            if (logado.getCongregacao() == null) {
                if (logado.getPerfil() != Perfil.SUPER_ADMIN) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                congregacaoId = logado.getCongregacao().getIdCongregacao().intValue();
            }
        }

        Map<String, BigDecimal> totais = new HashMap<>();
        totais.put("total", movimentacaoService.calcularTotalGeralPorCongregacao(congregacaoId));
        return ResponseEntity.ok(totais);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoResponseDTO> buscarMovimentacaoPorId(@PathVariable int id) {
        try {
            return ResponseEntity.ok(movimentacaoService.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/dizimoByUsuario")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorUsuario(
    	@RequestParam int id_usuario) {
        return ResponseEntity.ok(movimentacaoService.listarPorUsuario(id_usuario));
    }

    // Rota para adicionar uma nova movimentação (POST)
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody MovimentacaoDTO movimentacaoDTO) {
        try {
            Usuario logado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            MovimentacaoResponseDTO salva = movimentacaoService.criar(movimentacaoDTO, logado);
            return ResponseEntity.ok(salva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Rota para atualizar uma movimentação (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarMovimentacao(
            @PathVariable Integer id,
            @RequestBody MovimentacaoDTO movimentacaoDTO) {
        
        try {
            MovimentacaoResponseDTO resultado = movimentacaoService.atualizar(id, movimentacaoDTO);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Rota para excluir uma movimentação (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMovimentacao(@PathVariable int id) {
        try {
            movimentacaoService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
