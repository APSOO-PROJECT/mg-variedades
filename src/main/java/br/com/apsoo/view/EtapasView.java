package br.com.apsoo.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

// Indicador de passos de um assistente: círculos numerados ligados por linhas. O passo atual fica azul,
// os já feitos ficam verdes com um ✓ (e podem ser clicados para voltar). Em janelas estreitas só o
// nome do passo atual aparece.
public class EtapasView extends HBox {

    private static final double LIMITE_COMPACTO = 640;

    private final int total;
    private final List<StackPane> circulos = new ArrayList<>();
    private final List<Label> rotulos = new ArrayList<>();
    private final List<Region> linhas = new ArrayList<>();
    private IntConsumer aoEscolher = indice -> { };
    private int atual;
    private boolean concluido;

    public EtapasView(String... titulos) {
        super(10);
        this.total = titulos.length;
        setAlignment(Pos.CENTER);

        for (int i = 0; i < total; i++) {
            StackPane circulo = new StackPane();
            circulo.getStyleClass().add("etapa-circulo");
            circulos.add(circulo);

            Label rotulo = new Label(titulos[i]);
            rotulo.getStyleClass().add("etapa-texto");
            rotulos.add(rotulo);

            HBox item = new HBox(8, circulo, rotulo);
            item.setAlignment(Pos.CENTER_LEFT);
            final int indice = i;
            item.setOnMouseClicked(e -> {
                if (indice < atual && !concluido) {
                    aoEscolher.accept(indice);
                }
            });
            getChildren().add(item);

            if (i < total - 1) {
                Region linha = new Region();
                linha.getStyleClass().add("etapa-linha");
                linha.setMinWidth(14);
                HBox.setHgrow(linha, Priority.ALWAYS);
                linhas.add(linha);
                getChildren().add(linha);
            }
        }

        widthProperty().addListener((obs, antigo, largura) -> ajustarRotulos());
        definir(0, false);
    }

    // Chamado ao clicar em um passo já concluído (só é possível voltar)
    public void setAoEscolher(IntConsumer aoEscolher) {
        this.aoEscolher = aoEscolher;
    }

    public void definir(int atual, boolean concluido) {
        this.atual = atual;
        this.concluido = concluido;

        for (int i = 0; i < total; i++) {
            boolean feito = concluido || i < atual;
            boolean ativo = !concluido && i == atual;

            StackPane circulo = circulos.get(i);
            circulo.getStyleClass().removeAll("atual", "feita");
            circulo.getChildren().clear();
            if (feito) {
                circulo.getStyleClass().add("feita");
                circulo.getChildren().add(Icones.de("ativar", 18));
            } else {
                if (ativo) {
                    circulo.getStyleClass().add("atual");
                }
                circulo.getChildren().add(new Label(String.valueOf(i + 1)));
            }
            circulo.getParent().setStyle(feito && !concluido ? "-fx-cursor: hand;" : "");

            rotulos.get(i).getStyleClass().removeAll("atual", "feita");
            if (ativo) {
                rotulos.get(i).getStyleClass().add("atual");
            } else if (feito) {
                rotulos.get(i).getStyleClass().add("feita");
            }
        }
        for (int i = 0; i < linhas.size(); i++) {
            linhas.get(i).getStyleClass().remove("feita");
            if (concluido || i < atual) {
                linhas.get(i).getStyleClass().add("feita");
            }
        }
        ajustarRotulos();
    }

    private void ajustarRotulos() {
        boolean compacto = getWidth() > 0 && getWidth() < LIMITE_COMPACTO;
        for (int i = 0; i < total; i++) {
            boolean mostrar = !compacto || (!concluido && i == atual);
            rotulos.get(i).setVisible(mostrar);
            rotulos.get(i).setManaged(mostrar);
        }
    }
}
