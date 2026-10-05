package br.com.apsoo.view;

import java.util.Map;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

// Ícones de traço (24x24, estilo Feather) desenhados como SVG; a cor vem do CSS (.icone)
public final class Icones {

    private static final Map<String, String> CAMINHOS = Map.ofEntries(
            Map.entry("dashboard", "M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z"),
            Map.entry("vendas", "M8 21a1 1 0 1 0 2 0a1 1 0 1 0-2 0 M19 21a1 1 0 1 0 2 0a1 1 0 1 0-2 0 "
                    + "M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"),
            Map.entry("estoque", "M12 2L2 7l10 5 10-5-10-5z M2 17l10 5 10-5 M2 12l10 5 10-5"),
            Map.entry("produtos", "M16.5 9.4l-9-5.19 M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8"
                    + "a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.27 6.96L12 12.01l8.73-5.05 M12 22.08V12"),
            Map.entry("categorias", "M20.59 13.41l-7.18 7.18a2 2 0 0 1-2.82 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z M7 7h.01"),
            Map.entry("caixa", "M12 1v22 M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"),
            Map.entry("relatorios", "M18 20V10 M12 20V4 M6 20v-6"),
            Map.entry("usuarios", "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M5 7a4 4 0 1 0 8 0a4 4 0 1 0-8 0 "
                    + "M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75"),
            Map.entry("conta", "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M8 7a4 4 0 1 0 8 0a4 4 0 1 0-8 0"),
            Map.entry("sair", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9"),
            Map.entry("menu", "M3 12h18 M3 6h18 M3 18h18"),
            Map.entry("fechar", "M18 6L6 18 M6 6l12 12"),
            Map.entry("editar", "M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"),
            Map.entry("excluir", "M3 6h18 M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2 "
                    + "M10 11v6 M14 11v6"),
            Map.entry("historico", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0 M12 6v6l4 2"),
            Map.entry("desativar", "M18.36 6.64a9 9 0 1 1-12.73 0 M12 2v10"),
            Map.entry("ativar", "M20 6L9 17l-5-5"),
            Map.entry("promover", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0 M16 12l-4-4-4 4 M12 16V8"),
            Map.entry("buscar", "M3 11a8 8 0 1 0 16 0a8 8 0 1 0-16 0 M21 21l-4.35-4.35"),
            Map.entry("mais", "M12 5v14 M5 12h14"),
            Map.entry("menos", "M5 12h14"),
            Map.entry("atualizar", "M23 4v6h-6 M1 20v-6h6 M3.51 9a9 9 0 0 1 14.85-3.36L23 10 M1 14l4.64 4.36A9 9 0 0 0 20.49 15"),
            Map.entry("exportar", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4 M7 10l5 5 5-5 M12 15V3"),
            Map.entry("alerta", "M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z "
                    + "M12 9v4 M12 17h.01"),
            Map.entry("tendencia", "M23 6l-9.5 9.5-5-5L1 18 M17 6h6v6"),
            Map.entry("estrela", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"),
            Map.entry("recibo", "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8"),
            Map.entry("devolucao", "M1 4v6h6 M3.51 15a9 9 0 1 0 2.13-9.36L1 10"),
            Map.entry("confirmar", "M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4L12 14.01l-3-3"),
            Map.entry("cartao", "M3 4h18a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z M1 10h22"),
            Map.entry("cadeado", "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z M7 11V7a5 5 0 0 1 10 0v4"),
            Map.entry("janela", "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6 M15 3h6v6 M10 14L21 3"),
            Map.entry("tela", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z M3 9h18 M9 21V9"),
            Map.entry("configuracoes", "M9 12a3 3 0 1 0 6 0a3 3 0 1 0-6 0 "
                    + "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 "
                    + "1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06"
                    + "a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09"
                    + "A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9"
                    + "a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06"
                    + "a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09"
                    + "a1.65 1.65 0 0 0-1.51 1z"),
            Map.entry("calendario", "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z "
                    + "M16 2v4 M8 2v4 M3 10h18"));

    private Icones() {
    }

    public static Node de(String nome, double tamanho) {
        String caminho = CAMINHOS.get(nome);
        if (caminho == null) {
            throw new IllegalArgumentException("Ícone desconhecido: " + nome);
        }

        // O retângulo transparente fixa a caixa de 24x24, para todos os ícones terem o mesmo tamanho e alinhamento
        Rectangle area = new Rectangle(24, 24, Color.TRANSPARENT);
        SVGPath desenho = new SVGPath();
        desenho.setContent(caminho);
        desenho.getStyleClass().add("icone");

        Group interno = new Group(area, desenho);
        interno.setScaleX(tamanho / 24.0);
        interno.setScaleY(tamanho / 24.0);

        StackPane caixa = new StackPane(new Group(interno));
        caixa.setMinSize(tamanho, tamanho);
        caixa.setPrefSize(tamanho, tamanho);
        caixa.setMaxSize(tamanho, tamanho);
        caixa.setMouseTransparent(true);
        return caixa;
    }

    public static Node de(String nome) {
        return de(nome, 18);
    }
}
