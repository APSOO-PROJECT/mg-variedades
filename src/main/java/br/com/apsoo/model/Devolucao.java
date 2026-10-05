package br.com.apsoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Devolucao {
    private Integer id;
    private Venda venda;
    private Usuario usuario;
    private LocalDateTime dataHora;
    private CondicaoProduto condicaoProduto;
    private String motivo;
    private FormaPagamento tipoReembolso;
    private final List<ItemDevolucao> itens = new ArrayList<>();

    public Devolucao() {}

    public Devolucao(Venda venda, Usuario usuario, CondicaoProduto condicaoProduto,
                     String motivo, FormaPagamento tipoReembolso) {
        this.venda = venda;
        this.usuario = usuario;
        this.condicaoProduto = condicaoProduto;
        this.motivo = motivo;
        this.tipoReembolso = tipoReembolso;
    }

    public void adicionarItem(ItemVenda itemVenda, int quantidade) {
        itens.add(new ItemDevolucao(itemVenda, quantidade));
    }

    public BigDecimal calcularValorReembolsado() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemDevolucao item : itens) {
            total = total.add(item.calcularValor());
        }
        return total;
    }

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Venda getVenda() {
        return venda;
    }
    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public CondicaoProduto getCondicaoProduto() {
        return condicaoProduto;
    }
    public void setCondicaoProduto(CondicaoProduto condicaoProduto) {
        this.condicaoProduto = condicaoProduto;
    }

    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public FormaPagamento getTipoReembolso() {
        return tipoReembolso;
    }
    public void setTipoReembolso(FormaPagamento tipoReembolso) {
        this.tipoReembolso = tipoReembolso;
    }

    public List<ItemDevolucao> getItens() {
        return itens;
    }
}
