package br.com.apsoo.model;

import java.util.ArrayList;
import java.util.List;

// Relatório de produtos vendidos no período (RF20)
public class RelatorioProdutosVendidos {
    private ProdutoVendido produtoMaisVendido;
    private List<VendaProduto> maisVendidosPorCategoria = new ArrayList<>();
    private List<VendaProduto> semVenda = new ArrayList<>();

    public ProdutoVendido getProdutoMaisVendido() {
        return produtoMaisVendido;
    }
    public void setProdutoMaisVendido(ProdutoVendido produtoMaisVendido) {
        this.produtoMaisVendido = produtoMaisVendido;
    }

    public List<VendaProduto> getMaisVendidosPorCategoria() {
        return maisVendidosPorCategoria;
    }
    public void setMaisVendidosPorCategoria(List<VendaProduto> maisVendidosPorCategoria) {
        this.maisVendidosPorCategoria = maisVendidosPorCategoria;
    }

    public List<VendaProduto> getSemVenda() {
        return semVenda;
    }
    public void setSemVenda(List<VendaProduto> semVenda) {
        this.semVenda = semVenda;
    }
}
