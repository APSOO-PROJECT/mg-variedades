package br.com.apsoo.model;

public enum StatusProduto {
    DISPONIVEL("Disponível"), INDISPONIVEL("Indisponível");

    private final String descricao;

    StatusProduto(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
