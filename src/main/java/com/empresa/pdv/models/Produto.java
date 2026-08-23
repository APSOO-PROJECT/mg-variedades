package com.empresa.pdv.models;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Produto {
    private Long id;
    private String nome;
    private String categoria;
    private BigDecimal custoAquisicao;
    private BigDecimal precoVenda;
    private String codigoBarras;
    private int quantidadeMinima;
    private int quantidadeAtual;
    private LocalDate dataAdicao;
    private LocalDate dataAtualizacao;
    private StatusProduto status;
    private Usuario usuarioresponsavel;

    // Métodos Construtores
    public Produto() {
    }

    public Produto(Long id, String nome, String categoria, BigDecimal custoAquisicao, BigDecimal precoVenda,
                   String codigoBarras, int quantidadeMinima, int quantidadeAtual, LocalDate dataAdicao,
                   LocalDate dataAtualizacao, StatusProduto status, Usuario usuarioresponsavel) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.custoAquisicao = custoAquisicao;
        this.precoVenda = precoVenda;
        this.codigoBarras = codigoBarras;
        this.quantidadeMinima = quantidadeMinima;
        this.quantidadeAtual = quantidadeAtual;
        this.dataAdicao = LocalDate.now();
        this.dataAtualizacao = LocalDate.now();
        this.status = StatusProduto.DISPONIVEL;
        this.usuarioresponsavel = usuarioresponsavel;
    }

    public boolean isEstoqueBaixo() {
        if (quantidadeAtual <= quantidadeMinima) {
            return true;
        } else {
            return false;
        }
    }

    // Métodos Getters e Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    public BigDecimal getCustoAquisicao() {
        return custoAquisicao;
    }
    public void setCustoAquisicao(BigDecimal custoAquisicao) {
        this.custoAquisicao = custoAquisicao;
    }
    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }
    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }
    public String getCodigoBarras() {
        return codigoBarras;
    }
    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }
    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }
    public void setQuantidadeMinima(int quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }
    public int getQuantidadeAtual() {
        return quantidadeAtual;
    }
    public void setQuantidadeAtual(int quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }
    public LocalDate getDataAdicao() {
        return dataAdicao;
    }
    public void setDataAdicao(LocalDate dataAdicao) {
        this.dataAdicao = dataAdicao;
    }
    public LocalDate getDataAtualizacao() {
        return dataAtualizacao;
    }
    public void setDataAtualizacao(LocalDate dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    public StatusProduto getStatus() {
        return status;
    }
    public void setStatus(StatusProduto status) {
        this.status = status;
    }
    public Usuario getUsuarioresponsavel() {
        return usuarioresponsavel;
    }
    public void setUsuarioresponsavel(Usuario usuarioresponsavel) {
        this.usuarioresponsavel = usuarioresponsavel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
        return true;
        }
        if (!(o instanceof Produto produto)) {
            return false;
        }
        return id != null && id.equals(produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Produto{id=%d, nome='%s', categoria='%s', quantidadeAtual=%d}"
            .formatted(id, nome, categoria, quantidadeAtual);
    }
}

