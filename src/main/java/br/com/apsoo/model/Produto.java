package br.com.apsoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Produto {
    private Integer id;
    private String nome;
    private String codigoBarras;
    private BigDecimal custoAquisicao;
    private BigDecimal preco;
    private int quantidadeAtual;
    private int quantidadeMinima;
    private StatusProduto status;
    private Categoria categoria;
    private LocalDateTime dataAdicao;
    private LocalDateTime dataAtualizacao;
    private Integer usuarioAtualizacaoId;
    private String usuarioAtualizacaoNome; // preenchido apenas nas consultas

    public Produto() {}

    public Produto(Integer id, String nome, String codigoBarras, BigDecimal custoAquisicao,
                   BigDecimal preco, int quantidadeAtual, int quantidadeMinima,
                   StatusProduto status, Categoria categoria, LocalDateTime dataAdicao,
                   LocalDateTime dataAtualizacao, Integer usuarioAtualizacaoId) {
        this.id = id;
        this.nome = nome;
        this.codigoBarras = codigoBarras;
        this.custoAquisicao = custoAquisicao;
        this.preco = preco;
        this.quantidadeAtual = quantidadeAtual;
        this.quantidadeMinima = quantidadeMinima;
        this.status = status;
        this.categoria = categoria;
        this.dataAdicao = dataAdicao;
        this.dataAtualizacao = dataAtualizacao;
        this.usuarioAtualizacaoId = usuarioAtualizacaoId;
    }

    // Regra de Negócio: toda venda confirmada decrementa o estoque
    public void debitarEstoque(int quantidade) {
        if (quantidade > quantidadeAtual) {
            throw new IllegalArgumentException("Quantidade superior à disponível em estoque.");
        }
        this.quantidadeAtual -= quantidade;
    }

    // Regra de Negócio: produtos com a quantidade mínima atingida devem ser sinalizados
    public boolean sinalizarEstoqueBaixo() {
        return quantidadeAtual <= quantidadeMinima;
    }

    public boolean isDisponivel() {
        return status == StatusProduto.DISPONIVEL && quantidadeAtual > 0;
    }

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }
    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public BigDecimal getCustoAquisicao() {
        return custoAquisicao;
    }
    public void setCustoAquisicao(BigDecimal custoAquisicao) {
        this.custoAquisicao = custoAquisicao;
    }

    public BigDecimal getPreco() {
        return preco;
    }
    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public int getQuantidadeAtual() {
        return quantidadeAtual;
    }
    public void setQuantidadeAtual(int quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }
    public void setQuantidadeMinima(int quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public StatusProduto getStatus() {
        return status;
    }
    public void setStatus(StatusProduto status) {
        this.status = status;
    }

    public Categoria getCategoria() {
        return categoria;
    }
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getDataAdicao() {
        return dataAdicao;
    }
    public void setDataAdicao(LocalDateTime dataAdicao) {
        this.dataAdicao = dataAdicao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Integer getUsuarioAtualizacaoId() {
        return usuarioAtualizacaoId;
    }
    public void setUsuarioAtualizacaoId(Integer usuarioAtualizacaoId) {
        this.usuarioAtualizacaoId = usuarioAtualizacaoId;
    }

    public String getUsuarioAtualizacaoNome() {
        return usuarioAtualizacaoNome;
    }
    public void setUsuarioAtualizacaoNome(String usuarioAtualizacaoNome) {
        this.usuarioAtualizacaoNome = usuarioAtualizacaoNome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
