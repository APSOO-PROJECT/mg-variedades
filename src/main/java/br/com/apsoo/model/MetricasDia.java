package br.com.apsoo.model;

import java.math.BigDecimal;

// Métricas do painel inicial, desde a abertura do caixa vigente (RF17)
public class MetricasDia {
    private boolean caixaAberto;
    private BigDecimal faturamento = BigDecimal.ZERO;
    private int quantidadeVendas;
    private ProdutoVendido produtoMaisVendido;
    private int produtosEstoqueBaixo;

    public boolean isCaixaAberto() {
        return caixaAberto;
    }
    public void setCaixaAberto(boolean caixaAberto) {
        this.caixaAberto = caixaAberto;
    }

    public BigDecimal getFaturamento() {
        return faturamento;
    }
    public void setFaturamento(BigDecimal faturamento) {
        this.faturamento = faturamento;
    }

    public int getQuantidadeVendas() {
        return quantidadeVendas;
    }
    public void setQuantidadeVendas(int quantidadeVendas) {
        this.quantidadeVendas = quantidadeVendas;
    }

    public ProdutoVendido getProdutoMaisVendido() {
        return produtoMaisVendido;
    }
    public void setProdutoMaisVendido(ProdutoVendido produtoMaisVendido) {
        this.produtoMaisVendido = produtoMaisVendido;
    }

    public int getProdutosEstoqueBaixo() {
        return produtosEstoqueBaixo;
    }
    public void setProdutosEstoqueBaixo(int produtosEstoqueBaixo) {
        this.produtosEstoqueBaixo = produtosEstoqueBaixo;
    }
}
