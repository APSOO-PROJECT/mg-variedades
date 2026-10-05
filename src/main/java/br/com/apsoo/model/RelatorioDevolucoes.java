package br.com.apsoo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Relatório de devoluções do período (RF22)
public class RelatorioDevolucoes {

    // Categoria com a quantidade de itens devolvidos
    public record CategoriaDevolvida(String categoria, int quantidade) {
    }

    private int quantidadeDevolucoes;
    private int quantidadeItens;
    private BigDecimal valorReembolsado = BigDecimal.ZERO;
    private List<CategoriaDevolvida> rankingCategorias = new ArrayList<>();
    private final Map<CondicaoProduto, Integer> itensPorCondicao = new EnumMap<>(CondicaoProduto.class);
    private String motivoPredominante;

    public RelatorioDevolucoes() {
        for (CondicaoProduto condicao : CondicaoProduto.values()) {
            itensPorCondicao.put(condicao, 0);
        }
    }

    // Métodos Getters e Setters
    public int getQuantidadeDevolucoes() {
        return quantidadeDevolucoes;
    }
    public void setQuantidadeDevolucoes(int quantidadeDevolucoes) {
        this.quantidadeDevolucoes = quantidadeDevolucoes;
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }
    public void setQuantidadeItens(int quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }

    public BigDecimal getValorReembolsado() {
        return valorReembolsado;
    }
    public void setValorReembolsado(BigDecimal valorReembolsado) {
        this.valorReembolsado = valorReembolsado;
    }

    public List<CategoriaDevolvida> getRankingCategorias() {
        return rankingCategorias;
    }
    public void setRankingCategorias(List<CategoriaDevolvida> rankingCategorias) {
        this.rankingCategorias = rankingCategorias;
    }

    public Map<CondicaoProduto, Integer> getItensPorCondicao() {
        return itensPorCondicao;
    }

    public String getMotivoPredominante() {
        return motivoPredominante;
    }
    public void setMotivoPredominante(String motivoPredominante) {
        this.motivoPredominante = motivoPredominante;
    }
}
