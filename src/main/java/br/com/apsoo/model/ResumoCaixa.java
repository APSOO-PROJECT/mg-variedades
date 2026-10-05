package br.com.apsoo.model;

import java.math.BigDecimal;

// Totais de um caixa, usados na consulta e no fechamento
public class ResumoCaixa {
    private BigDecimal fundoTrocoInicial = BigDecimal.ZERO;
    private BigDecimal vendasDinheiro = BigDecimal.ZERO;
    private BigDecimal vendasPix = BigDecimal.ZERO;
    private BigDecimal vendasCartao = BigDecimal.ZERO;
    private BigDecimal suprimentos = BigDecimal.ZERO;
    private BigDecimal sangrias = BigDecimal.ZERO;
    private BigDecimal reembolsosDinheiro = BigDecimal.ZERO;
    private BigDecimal lucro = BigDecimal.ZERO;

    // Valor esperado em caixa (RF10):
    // fundo de troco + vendas em dinheiro + suprimentos - sangrias - reembolsos em dinheiro
    public BigDecimal calcularSaldoEsperado() {
        return fundoTrocoInicial
                .add(vendasDinheiro)
                .add(suprimentos)
                .subtract(sangrias)
                .subtract(reembolsosDinheiro);
    }

    public BigDecimal calcularTotalVendas() {
        return vendasDinheiro.add(vendasPix).add(vendasCartao);
    }

    // Métodos Getters e Setters
    public BigDecimal getFundoTrocoInicial() {
        return fundoTrocoInicial;
    }
    public void setFundoTrocoInicial(BigDecimal fundoTrocoInicial) {
        this.fundoTrocoInicial = fundoTrocoInicial;
    }

    public BigDecimal getVendasDinheiro() {
        return vendasDinheiro;
    }
    public void setVendasDinheiro(BigDecimal vendasDinheiro) {
        this.vendasDinheiro = vendasDinheiro;
    }

    public BigDecimal getVendasPix() {
        return vendasPix;
    }
    public void setVendasPix(BigDecimal vendasPix) {
        this.vendasPix = vendasPix;
    }

    public BigDecimal getVendasCartao() {
        return vendasCartao;
    }
    public void setVendasCartao(BigDecimal vendasCartao) {
        this.vendasCartao = vendasCartao;
    }

    public BigDecimal getSuprimentos() {
        return suprimentos;
    }
    public void setSuprimentos(BigDecimal suprimentos) {
        this.suprimentos = suprimentos;
    }

    public BigDecimal getSangrias() {
        return sangrias;
    }
    public void setSangrias(BigDecimal sangrias) {
        this.sangrias = sangrias;
    }

    public BigDecimal getReembolsosDinheiro() {
        return reembolsosDinheiro;
    }
    public void setReembolsosDinheiro(BigDecimal reembolsosDinheiro) {
        this.reembolsosDinheiro = reembolsosDinheiro;
    }

    public BigDecimal getLucro() {
        return lucro;
    }
    public void setLucro(BigDecimal lucro) {
        this.lucro = lucro;
    }
}
