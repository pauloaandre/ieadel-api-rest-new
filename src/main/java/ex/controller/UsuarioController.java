package ex.controller;

import ex.infra.security.TokenService;
import ex.model.*;
import ex.model.repository.UsuarioRepository;
import ex.model.repository.CongregacaoRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


import ex.service.UsuarioService;

@RestController
@RequestMapping("/auth")
public class UsuarioController {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CongregacaoRepository congregacaoRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/search")
    public List<UsuarioResponseDTO> buscarUsuariosPorNome(@RequestParam String nome) {
        return usuarioService.buscarUsuariosPorNomeNaCongregacao(nome);
    }

    @GetMapping
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findByAtivoTrue().stream()
                .map(UsuarioResponseDTO::fromUsuario)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> ResponseEntity.ok(UsuarioResponseDTO.fromUsuario(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data, HttpServletResponse response) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var usuario = (Usuario) auth.getPrincipal();

        var token = tokenService.generateToken(usuario);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("None")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new LoginResponseDTO(token, UsuarioResponseDTO.fromUsuario(usuario)));
    }

    @PostMapping("/novo")
    public ResponseEntity criar(@RequestBody @Valid RegisterDTO data) {
        if (this.usuarioRepository.findByEmail(data.email()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());
        // Força o perfil USER para autocadastro
        Usuario usuario = new Usuario(data.nome(), data.email(), encryptedPassword, Perfil.USER);
        
        Congregacao congregacao = congregacaoRepository.findById(data.idCongregacao())
                .orElseThrow(() -> new IllegalArgumentException("Congregação não encontrada"));
        usuario.setCongregacao(congregacao);
        
        usuario.setAtivo(true);
        this.usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Usuário registrado com sucesso."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    usuarioExistente.setNome(usuarioAtualizado.getNome());
                    usuarioExistente.setEmail(usuarioAtualizado.getEmail());
                    // A senha não deve ser atualizada por aqui sem criptografia!
                    usuarioRepository.save(usuarioExistente);
                    return ResponseEntity.ok(UsuarioResponseDTO.fromUsuario(usuarioExistente));
                })
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
