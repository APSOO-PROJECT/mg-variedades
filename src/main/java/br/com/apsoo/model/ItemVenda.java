package br.com.apsoo.model;

import java.math.BigDecimal;

public class ItemVenda {
    private Integer id;
    private Produto produto;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal custoUnitario; // custo no momento da venda, usado no cálculo de lucro
    private int quantidadeDevolvida;

    public ItemVenda() {}

    public ItemVenda(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
        this.custoUnitario = produto.getCustoAquisicao();
    }

    public BigDecimal calcularSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public int quantidadeDevolvivel() {
        return quantidade - quantidadeDevolvida;
    }

    // Métodos Getters e Setters
    public int getQuantidadeDevolvida() {
        return quantidadeDevolvida;
    }
    public void setQuantidadeDevolvida(int quantidadeDevolvida) {
        this.quantidadeDevolvida = quantidadeDevolvida;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }
    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }
    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }
}
