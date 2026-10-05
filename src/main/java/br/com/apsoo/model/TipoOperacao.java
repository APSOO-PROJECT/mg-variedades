package br.com.apsoo.model;

public enum TipoOperacao {
    SANGRIA("Sangria"), SUPRIMENTO("Suprimento"), REEMBOLSO("Reembolso");

    private final String descricao;

    TipoOperacao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
