package br.com.apsoo.model;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"), PIX("Pix"), CARTAO("Cartão");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
