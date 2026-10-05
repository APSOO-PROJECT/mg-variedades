package br.com.apsoo.model;

import java.math.BigDecimal;

// Vendas de uma categoria em um período; categorias sem venda aparecem zeradas
public class LinhaVendaCategoria {
    private final Integer categoriaId;
    private final String categoriaNome;
    private final int quantidade;
    private final BigDecimal valor;
    private ProdutoVendido maisVendido;
    private ProdutoVendido menosVendido;
    private boolean alteradaNoPeriodo;

    public LinhaVendaCategoria(Integer categoriaId, String categoriaNome, int quantidade, BigDecimal valor) {
        this.categoriaId = categoriaId;
        this.categoriaNome = categoriaNome;
        this.quantidade = quantidade;
        this.valor = valor;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public ProdutoVendido getMaisVendido() {
        return maisVendido;
    }
    public void setMaisVendido(ProdutoVendido maisVendido) {
        this.maisVendido = maisVendido;
    }

    public ProdutoVendido getMenosVendido() {
        return menosVendido;
    }
    public void setMenosVendido(ProdutoVendido menosVendido) {
        this.menosVendido = menosVendido;
    }

    public boolean isAlteradaNoPeriodo() {
        return alteradaNoPeriodo;
    }
    public void setAlteradaNoPeriodo(boolean alteradaNoPeriodo) {
        this.alteradaNoPeriodo = alteradaNoPeriodo;
    }
}
