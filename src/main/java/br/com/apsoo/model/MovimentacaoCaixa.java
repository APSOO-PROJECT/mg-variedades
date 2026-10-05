package br.com.apsoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimentacaoCaixa {
    private Integer id;
    private Caixa caixa;
    private Usuario usuario;
    private TipoOperacao tipoOperacao;
    private BigDecimal valor;
    private String motivo;
    private LocalDateTime dataHora;

    public MovimentacaoCaixa() {}

    public MovimentacaoCaixa(Caixa caixa, Usuario usuario, TipoOperacao tipoOperacao,
                             BigDecimal valor, String motivo) {
        this.caixa = caixa;
        this.usuario = usuario;
        this.tipoOperacao = tipoOperacao;
        this.valor = valor;
        this.motivo = motivo;
    }

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Caixa getCaixa() {
        return caixa;
    }
    public void setCaixa(Caixa caixa) {
        this.caixa = caixa;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoOperacao getTipoOperacao() {
        return tipoOperacao;
    }
    public void setTipoOperacao(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }

    public BigDecimal getValor() {
        return valor;
    }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
