package br.com.apsoo.model;

import java.math.BigDecimal;

public class Cartao extends Pagamento {
    private ModalidadeCartao modalidade;
    private BigDecimal taxa = BigDecimal.ZERO;
    private int parcelas;

    public Cartao(BigDecimal valorVenda, ModalidadeCartao modalidade, int parcelas) {
        super(valorVenda, valorVenda);
        this.modalidade = modalidade;
        this.parcelas = parcelas;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.CARTAO;
    }

    public ModalidadeCartao getModalidade() {
        return modalidade;
    }
    public void setModalidade(ModalidadeCartao modalidade) {
        this.modalidade = modalidade;
    }

    public BigDecimal getTaxa() {
        return taxa;
    }
    public void setTaxa(BigDecimal taxa) {
        this.taxa = taxa;
    }

    public int getParcelas() {
        return parcelas;
    }
    public void setParcelas(int parcelas) {
        this.parcelas = parcelas;
    }
}
