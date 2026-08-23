package com.empresa.pdv.models;
import java.math.BigDecimal;

public class ItemVenda {
    private Produto produto;
    private int quantidade;
    private BigDecimal precoUnitario;

    // Métodos Construtores
    public ItemVenda(Produto produto, int quantidade, BigDecimal precoUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    // Método Get Subtotal
    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    // Métodos Getters
    public Produto getProduto() {
        return produto;
    }
    public int getQuantidade() {
        return quantidade;
    }
    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    @Override
    public String toString() {
        return "ItemVenda{" +
                "produto=" + produto.getNome() +
                ", quantidade=" + quantidade +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
