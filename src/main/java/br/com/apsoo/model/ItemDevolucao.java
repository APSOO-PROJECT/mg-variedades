package br.com.apsoo.model;

import java.math.BigDecimal;

public class ItemDevolucao {
    private Integer id;
    private ItemVenda itemVenda;
    private int quantidade;

    public ItemDevolucao() {}

    public ItemDevolucao(ItemVenda itemVenda, int quantidade) {
        this.itemVenda = itemVenda;
        this.quantidade = quantidade;
    }

    // O reembolso usa o preço pago na venda, não o preço atual do produto
    public BigDecimal calcularValor() {
        return itemVenda.getPrecoUnitario().multiply(BigDecimal.valueOf(quantidade));
    }

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public ItemVenda getItemVenda() {
        return itemVenda;
    }
    public void setItemVenda(ItemVenda itemVenda) {
        this.itemVenda = itemVenda;
    }

    public int getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
