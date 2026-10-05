package br.com.apsoo.model;

// Onde abrem os formulários e as mensagens das ações (preferência de cada usuário)
public enum ModoAcao {
    TELA("Na própria tela", "O formulário aparece sobre a página atual, sem abrir outra janela."),
    JANELA("Em uma janela separada", "O formulário abre em uma janela própria, à parte da tela principal.");

    private final String titulo;
    private final String descricao;

    ModoAcao(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
