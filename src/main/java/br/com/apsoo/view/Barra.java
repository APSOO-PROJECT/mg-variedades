package br.com.apsoo.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

// Barra de ferramentas responsiva: filtros à esquerda e ações à direita.
// Se não couberem lado a lado, as ações descem para a linha de baixo, alinhadas à direita;
// os grupos também quebram linha internamente quando a janela é estreita.
public class Barra extends Pane {

    private static final double ESPACO = 12;

    private final FlowPane esquerda = new Fluxo(ESPACO, ESPACO);
    private final FlowPane direita = new Fluxo(ESPACO, ESPACO);

    public Barra() {
        esquerda.setAlignment(Pos.CENTER_LEFT);
        direita.setAlignment(Pos.CENTER_RIGHT);
        getChildren().addAll(esquerda, direita);
    }

    public Barra aEsquerda(Node... itens) {
        esquerda.getChildren().addAll(itens);
        return this;
    }

    public Barra aDireita(Node... itens) {
        direita.getChildren().addAll(itens);
        return this;
    }

    // Largura do grupo se todos os itens ficassem na mesma linha (o prefWidth do FlowPane é a largura de quebra)
    private double larguraEmLinha(FlowPane grupo) {
        double soma = 0;
        int itens = 0;
        for (Node item : grupo.getChildren()) {
            if (item.isManaged()) {
                soma += item.prefWidth(-1);
                itens++;
            }
        }
        return soma + ESPACO * Math.max(0, itens - 1) + 2; // 2 px de folga contra arredondamentos
    }

    private boolean temDireita() {
        return !direita.getChildren().isEmpty();
    }

    private boolean lado(double largura) {
        double necessario = larguraEmLinha(esquerda) + (temDireita() ? ESPACO + larguraEmLinha(direita) : 0);
        return necessario <= largura;
    }

    @Override
    protected double computeMinWidth(double altura) {
        return 0;
    }

    @Override
    protected double computePrefWidth(double altura) {
        return larguraEmLinha(esquerda) + (temDireita() ? ESPACO + larguraEmLinha(direita) : 0);
    }

    @Override
    protected double computePrefHeight(double largura) {
        double w = largura > 0 ? largura : (getWidth() > 0 ? getWidth() : computePrefWidth(-1));
        if (lado(w)) {
            return Math.max(esquerda.prefHeight(larguraEmLinha(esquerda)),
                    temDireita() ? direita.prefHeight(larguraEmLinha(direita)) : 0);
        }
        double altura = esquerda.prefHeight(w);
        if (temDireita()) {
            altura += ESPACO + direita.prefHeight(Math.min(w, larguraEmLinha(direita)));
        }
        return altura;
    }

    @Override
    protected double computeMinHeight(double largura) {
        return computePrefHeight(largura);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double wDireita = temDireita() ? larguraEmLinha(direita) : 0;

        if (lado(w)) {
            double h = getHeight();
            esquerda.resizeRelocate(0, 0, w - wDireita - (temDireita() ? ESPACO : 0), h);
            direita.resizeRelocate(w - wDireita, 0, wDireita, h);
            return;
        }

        double hEsquerda = esquerda.prefHeight(w);
        esquerda.resizeRelocate(0, 0, w, hEsquerda);
        if (temDireita()) {
            double larguraDireita = Math.min(w, wDireita);
            direita.resizeRelocate(w - larguraDireita, hEsquerda + ESPACO, larguraDireita,
                    direita.prefHeight(larguraDireita));
        }
    }
}
