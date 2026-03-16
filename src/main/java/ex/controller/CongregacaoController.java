package ex.controller;

import ex.model.Congregacao;
import ex.model.CongregacaoResponseDTO;
import ex.model.repository.CongregacaoRepository;
import ex.service.CongregacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/congregacoes")
public class CongregacaoController {
    @Autowired
    private CongregacaoRepository congregacaoRepository;

    @Autowired
    private CongregacaoService congregacaoService;

    @GetMapping
    public List<CongregacaoResponseDTO> getCongregacao(Authentication authentication){
        boolean isSuperAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        return congregacaoRepository.findAll().stream()
                .map(congregacao -> CongregacaoResponseDTO.fromCongregacao(congregacao, isSuperAdmin))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Congregacao> criarCongregacao(@RequestBody Congregacao congregacao) {
        Congregacao novaCongregacao = congregacaoService.criarCongregacao(congregacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCongregacao);
    }
}
