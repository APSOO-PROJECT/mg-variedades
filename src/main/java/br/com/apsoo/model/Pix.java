package br.com.apsoo.model;

import java.math.BigDecimal;

public class Pix extends Pagamento {
    private String comprovante;

    public Pix(BigDecimal valorVenda, String comprovante) {
        super(valorVenda, valorVenda);
        this.comprovante = comprovante;
    }

    @Override
    public FormaPagamento getFormaPagamento() {
        return FormaPagamento.PIX;
    }

    public String getComprovante() {
        return comprovante;
    }
    public void setComprovante(String comprovante) {
        this.comprovante = comprovante;
    }
}
