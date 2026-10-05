package br.com.apsoo.model;

import java.math.BigDecimal;

public class Dinheiro extends Pagamento {

    public Dinheiro(BigDecimal valorVenda, BigDecimal valorRecebido) {
        super(valorVenda, valorRecebido);
    }

    // Regra de Negócio: o valor entregue deve cobrir o total da venda
    public boolean isValorSuficiente() {
        return getValorRecebido().compareTo(getValorVenda()) >= 0;
    }

    public BigDecimal calcularTroco() {
        return getValorRecebido().subtract(getValorVenda());
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.DINHEIRO;
    }
}
