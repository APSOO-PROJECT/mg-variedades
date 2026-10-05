package br.com.apsoo.view;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

// Fábrica de botões com ícone no padrão visual do sistema
public final class Botoes {

    private Botoes() {
    }

    // Botão com texto e ícone; estilos: "accent", "success", "danger", "ghost"
    public static Button comIcone(String texto, String icone, String... estilos) {
        Button botao = new Button(texto);
        botao.setGraphic(Icones.de(icone, 16));
        botao.getStyleClass().addAll(estilos);
        botao.setMnemonicParsing(false);
        return botao;
    }

    // Botão só com ícone e dica, usado nas linhas das tabelas
    public static Button acao(String icone, String dica, Runnable acao) {
        Button botao = new Button();
        botao.setGraphic(Icones.de(icone, 16));
        botao.getStyleClass().addAll("icone-btn", "ghost");
        botao.setTooltip(new Tooltip(dica));
        botao.setOnAction(e -> acao.run());
        return botao;
    }

    // Variante destrutiva do botão de ação (vermelho)
    public static Button acaoPerigosa(String icone, String dica, Runnable acao) {
        Button botao = acao(icone, dica, acao);
        botao.getStyleClass().add("perigo");
        return botao;
    }
}
