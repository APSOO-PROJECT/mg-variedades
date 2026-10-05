package br.com.apsoo.model;

public enum Cargo {
    ADMIN("Administrador"), VENDEDOR("Vendedor");

    private final String descricao;

    Cargo(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
