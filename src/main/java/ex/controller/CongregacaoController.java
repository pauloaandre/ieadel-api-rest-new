package ex.controller;

import ex.model.Congregacao;
import ex.model.repository.CongregacaoRepository;
import ex.service.CongregacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/congregacoes")
public class CongregacaoController {
    @Autowired
    private CongregacaoRepository congregacaoRepository;

    @Autowired
    private CongregacaoService congregacaoService;

    @GetMapping
    public List<Congregacao> getCongregacao(){
        return congregacaoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Congregacao> criarCongregacao(@RequestBody Congregacao congregacao) {
        Congregacao novaCongregacao = congregacaoService.criarCongregacao(congregacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCongregacao);
    }
}
