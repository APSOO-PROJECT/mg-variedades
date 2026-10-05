package br.com.apsoo.model;

import java.math.BigDecimal;

public abstract class Pagamento {
    private Integer id;
    private BigDecimal valorVenda;
    private BigDecimal valorRecebido;

    protected Pagamento(BigDecimal valorVenda, BigDecimal valorRecebido) {
        this.valorVenda = valorVenda;
        this.valorRecebido = valorRecebido;
    }

    public abstract FormaPagamento getFormaPagamento();

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }
    public void setValorVenda(BigDecimal valorVenda) {
        this.valorVenda = valorVenda;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }
    public void setValorRecebido(BigDecimal valorRecebido) {
        this.valorRecebido = valorRecebido;
    }
}
