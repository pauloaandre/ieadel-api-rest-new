package ex.service;

import ex.model.*;
import ex.model.repository.MovimentacaoRepository;
import ex.model.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MovimentacaoService {

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Listar por mês, ano e congregação (retornando DTOs)
    public List<MovimentacaoResponseDTO> listarPorMesAnoECongregacao(TipoMovimentacao tipo, String mes, String ano, int idCongregacao) {
        String mesStr = mes.length() == 1 ? "0" + mes : mes;
        return movimentacaoRepository.findByMesAnoECongregacao(tipo, mesStr, ano, idCongregacao)
                .stream()
                .map(MovimentacaoResponseDTO::fromMovimentacao)
                .toList();
    }

    // Listar por usuário (retornando DTOs)
    public List<MovimentacaoResponseDTO> listarPorUsuario(int usuarioId) {
        return movimentacaoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(MovimentacaoResponseDTO::fromMovimentacao)
                .toList();
    }

    // Buscar por ID (retornando DTO)
    public MovimentacaoResponseDTO buscarPorId(int id) {
        return movimentacaoRepository.findById(id)
                .map(MovimentacaoResponseDTO::fromMovimentacao)
                .orElseThrow(() -> new IllegalArgumentException("Movimentação não encontrada com ID: " + id));
    }

    // Adicionar movimentação (partindo de DTO e Usuario logado)
    @Transactional
    public MovimentacaoResponseDTO criar(MovimentacaoDTO dto, Usuario logado) {
        if (dto.getTipo() == TipoMovimentacao.DIZIMO && dto.getUsuarioId() == null && (dto.getIsVisitante() == null || !dto.getIsVisitante())) {
            throw new IllegalArgumentException("Dízimos requerem usuarioId ou flag isVisitante");
        }

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setDescricao(dto.getDescricao());
        movimentacao.setValor(dto.getValor());
        movimentacao.setData(dto.getData());
        movimentacao.setTipo(dto.getTipo());
        
        // Multi-tenant: Sempre usa a congregação do usuário que está operando
        movimentacao.setCongregacao(logado.getCongregacao());

        if (dto.getIsVisitante() != null && dto.getIsVisitante()) {
            Usuario visitante = usuarioRepository.findVisitanteByCongregacao(logado.getCongregacao().getIdCongregacao());
            if (visitante == null) {
                throw new IllegalArgumentException("Usuário Visitante não encontrado para esta congregação");
            }
            movimentacao.setUsuario(visitante);
        } else if (dto.getUsuarioId() != null) {
            Usuario usuarioDizimista = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário dizimista não encontrado"));
            movimentacao.setUsuario(usuarioDizimista);
        }

        Movimentacao salva = movimentacaoRepository.save(movimentacao);
        return MovimentacaoResponseDTO.fromMovimentacao(salva);
    }

    // Atualizar movimentação (partindo de DTO)
    @Transactional
    public MovimentacaoResponseDTO atualizar(Integer id, MovimentacaoDTO dto) {
        Movimentacao existente = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimentação não encontrada com ID: " + id));

        if (dto.getTipo() == TipoMovimentacao.DIZIMO && dto.getUsuarioId() == null && (dto.getIsVisitante() == null || !dto.getIsVisitante())) {
            throw new IllegalArgumentException("Dízimos requerem usuarioId ou flag isVisitante");
        }

        existente.setDescricao(dto.getDescricao());
        existente.setValor(dto.getValor());
        existente.setData(dto.getData());
        existente.setTipo(dto.getTipo());

        if (dto.getIsVisitante() != null && dto.getIsVisitante()) {
            Usuario visitante = usuarioRepository.findVisitanteByCongregacao(existente.getCongregacao().getIdCongregacao());
            if (visitante == null) {
                throw new IllegalArgumentException("Usuário Visitante não encontrado para esta congregação");
            }
            existente.setUsuario(visitante);
        } else if (dto.getUsuarioId() != null) {
            Usuario usuarioDizimista = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário dizimista não encontrado"));
            existente.setUsuario(usuarioDizimista);
        } else {
            existente.setUsuario(null);
        }

        Movimentacao atualizada = movimentacaoRepository.save(existente);
        return MovimentacaoResponseDTO.fromMovimentacao(atualizada);
    }

    // Calcular total por tipo e congregação (mensal)
    public BigDecimal calcularTotalECongregacao(TipoMovimentacao tipo, String mes, String ano, int idCongregacao) {
        BigDecimal total = movimentacaoRepository.calcularTotalPorTipoECongregacao(tipo, mes.length() == 1 ? "0" + mes : mes, ano, idCongregacao);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Calcular total geral por congregação
    public BigDecimal calcularTotalGeralPorCongregacao(int idCongregacao) {
        BigDecimal totalDizimos = movimentacaoRepository.getTotalPorTipoECongregacao(TipoMovimentacao.DIZIMO, idCongregacao);
        BigDecimal totalOfertas = movimentacaoRepository.getTotalPorTipoECongregacao(TipoMovimentacao.OFERTA, idCongregacao);
        BigDecimal totalDespesas = movimentacaoRepository.getTotalPorTipoECongregacao(TipoMovimentacao.DESPESA, idCongregacao);
        
        return (totalDizimos != null ? totalDizimos : BigDecimal.ZERO)
                .add(totalOfertas != null ? totalOfertas : BigDecimal.ZERO)
                .subtract(totalDespesas != null ? totalDespesas : BigDecimal.ZERO);
    }

    // Excluir movimentação
    @Transactional
    public void excluir(int id) {
        if (!movimentacaoRepository.existsById(id)) {
            throw new IllegalArgumentException("Movimentação não encontrada com ID: " + id);
        }
        movimentacaoRepository.deleteById(id);
    }
}
