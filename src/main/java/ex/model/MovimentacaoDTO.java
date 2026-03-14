package ex.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MovimentacaoDTO {
    private String descricao;
    private BigDecimal valor;
    private LocalDate data;
    private TipoMovimentacao tipo; 
    private Long usuarioId;
    private Boolean isVisitante;

    // Getters e Setters
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Boolean getIsVisitante() {
        return isVisitante;
    }

    public void setIsVisitante(Boolean isVisitante) {
        this.isVisitante = isVisitante;
    }
}
