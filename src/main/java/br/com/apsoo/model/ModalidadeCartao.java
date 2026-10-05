package br.com.apsoo.model;

public enum ModalidadeCartao {
    DEBITO("Débito"), CREDITO("Crédito");

    private final String descricao;

    ModalidadeCartao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
