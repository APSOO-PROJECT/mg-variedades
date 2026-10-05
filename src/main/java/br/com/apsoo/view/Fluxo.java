package br.com.apsoo.view;

import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

// FlowPane cuja largura preferida é a de todos os itens em uma única linha (o padrão do JavaFX é uma
// largura de quebra fixa de 400 px, o que faz grupos aninhados quebrarem cedo demais)
public class Fluxo extends FlowPane {

    public Fluxo(double espacoHorizontal, double espacoVertical, Node... itens) {
        super(espacoHorizontal, espacoVertical, itens);
    }

    @Override
    protected double computePrefWidth(double altura) {
        double soma = 0;
        int quantidade = 0;
        for (Node item : getChildren()) {
            if (item.isManaged()) {
                soma += item.prefWidth(-1);
                quantidade++;
            }
        }
        return snappedLeftInset() + soma + getHgap() * Math.max(0, quantidade - 1) + snappedRightInset();
    }
}
