package br.com.apsoo.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Relatório de vendas por período (RF15)
public class RelatorioVendas {
    private BigDecimal valorTotal = BigDecimal.ZERO;
    private int quantidadeVendas;
    private ProdutoVendido produtoMaisVendido;
    private List<LinhaVendaCategoria> categorias = new ArrayList<>();
    private final Map<FormaPagamento, BigDecimal> vendasPorForma = new EnumMap<>(FormaPagamento.class);

    public RelatorioVendas() {
        for (FormaPagamento forma : FormaPagamento.values()) {
            vendasPorForma.put(forma, BigDecimal.ZERO);
        }
    }

    // Média dos valores vendidos: valor total dividido pelo número de vendas
    public BigDecimal calcularMedia() {
        if (quantidadeVendas == 0) {
            return BigDecimal.ZERO;
        }
        return valorTotal.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP);
    }

    // Métodos Getters e Setters
    public BigDecimal getValorTotal() {
        return valorTotal;
    }
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
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

    public List<LinhaVendaCategoria> getCategorias() {
        return categorias;
    }
    public void setCategorias(List<LinhaVendaCategoria> categorias) {
        this.categorias = categorias;
    }

    public Map<FormaPagamento, BigDecimal> getVendasPorForma() {
        return vendasPorForma;
    }
}
