package com.empresa.pdv.models;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Venda {
    private Long id;
    private List<ItemVenda> itemVenda;
    private BigDecimal valorTotal;
    private MetodoPagamento metodoPagamento;
    private String nomeCliente;
    private LocalDateTime dataHoraVenda;
    private Usuario vendedorResponsavel;
    private Caixa caixaVinculado;

    // Métodos Construtores
    public Venda(Long id,
            List<ItemVenda> itens,
            MetodoPagamento metodoPagamento,
            String nomeCliente,
            LocalDateTime dataHora,
            Usuario vendedorResponsavel,
            Caixa caixaVinculado) {

        this.id = id;
        this.itemVenda = List.copyOf(itens);

        this.valorTotal = this.itemVenda.stream()
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.metodoPagamento = metodoPagamento;
        this.nomeCliente = nomeCliente;
        this.dataHoraVenda = dataHora;
        this.vendedorResponsavel = vendedorResponsavel;
        this.caixaVinculado = caixaVinculado;
    }

    // Métodos Getters
    public Long getId() {
        return id;
    }
    public List<ItemVenda> getItemVenda() {
        return itemVenda;
    }
    public BigDecimal getValorTotal() {
        return valorTotal;
    }
    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }
    public String getNomeCliente() {
        return nomeCliente;
    }
    public LocalDateTime getDataHoraVenda() {
        return dataHoraVenda;
    }
    public Usuario getVendedorResponsavel() {
        return vendedorResponsavel;
    }
    public Caixa getCaixaVinculado() {
        return caixaVinculado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venda venda)) return false;

        return id != null ? id.equals(venda.id) : venda.id == null;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Venda{" + id + ", valorTotal=" + valorTotal + ", metodoPagamento=" + metodoPagamento + ", dataHora=" + dataHoraVenda + '}';
    }

}
