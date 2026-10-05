package br.com.apsoo.controller;

public enum FormatoExportacao {
    PDF("PDF", "pdf"), CSV("Planilha CSV (Excel)", "csv");

    private final String descricao;
    private final String extensao;

    FormatoExportacao(String descricao, String extensao) {
        this.descricao = descricao;
        this.extensao = extensao;
    }

    public String getExtensao() {
        return extensao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
