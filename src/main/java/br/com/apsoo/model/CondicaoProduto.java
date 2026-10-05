package br.com.apsoo.model;

public enum CondicaoProduto {
    INTEGRAL("Funcionando integralmente"), PARCIAL("Funcionando parcialmente");

    private final String descricao;

    CondicaoProduto(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
