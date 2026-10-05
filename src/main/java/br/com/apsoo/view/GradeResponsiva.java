package br.com.apsoo.view;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

// Grade que reorganiza as colunas conforme a largura disponível (como "auto-fit" do CSS Grid):
// cada item tem uma largura mínima; cabem quantas colunas for possível e as demais linhas descem.
// Itens menores preenchem lacunas deixadas por itens largos (empacotamento denso).
public class GradeResponsiva extends Pane {

    private static final String PROPRIEDADE_COLUNAS = "gradeResponsiva.colunas";

    private final double larguraMinima;
    private final double espaco;

    public GradeResponsiva(double larguraMinima, double espaco) {
        this.larguraMinima = larguraMinima;
        this.espaco = espaco;
    }

    // Faz o item ocupar mais de uma coluna quando houver espaço (por exemplo, um campo largo)
    public static void ocupar(Node item, int colunas) {
        item.getProperties().put(PROPRIEDADE_COLUNAS, colunas);
    }

    private static int colunasDe(Node item) {
        Object valor = item.getProperties().get(PROPRIEDADE_COLUNAS);
        return valor instanceof Integer inteiro ? inteiro : 1;
    }

    private List<Node> itens() {
        List<Node> itens = new ArrayList<>();
        for (Node filho : getChildren()) {
            if (filho.isManaged()) {
                itens.add(filho);
            }
        }
        return itens;
    }

    private int totalColunas(double largura, int quantidadeItens) {
        int colunas = (int) Math.floor((largura + espaco) / (larguraMinima + espaco));
        int maximo = 0;
        for (Node item : itens()) {
            maximo += colunasDe(item);
        }
        colunas = Math.max(1, Math.min(colunas, Math.max(quantidadeItens, maximo)));
        if (maximo == quantidadeItens && colunas < quantidadeItens) {
            // Equilibra as linhas (4 itens em 3 colunas viram 2+2 em vez de 3+1)
            int linhas = (int) Math.ceil(quantidadeItens / (double) colunas);
            colunas = (int) Math.ceil(quantidadeItens / (double) linhas);
        }
        return colunas;
    }

    @Override
    protected double computeMinWidth(double altura) {
        return snappedLeftInset() + larguraMinima + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double altura) {
        List<Node> itens = itens();
        double soma = 0;
        for (Node item : itens) {
            soma += larguraMinima * colunasDe(item);
        }
        return snappedLeftInset() + soma + espaco * Math.max(0, itens.size() - 1) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double largura) {
        double larguraUtil = (largura > 0 ? largura : (getWidth() > 0 ? getWidth() : computePrefWidth(-1)))
                - snappedLeftInset() - snappedRightInset();
        return snappedTopInset() + distribuir(larguraUtil, false) + snappedBottomInset();
    }

    @Override
    protected double computeMinHeight(double largura) {
        return computePrefHeight(largura);
    }

    @Override
    protected void layoutChildren() {
        distribuir(getWidth() - snappedLeftInset() - snappedRightInset(), true);
    }

    // Empacota os itens nas primeiras linhas com espaço livre; com aplicar=true também posiciona.
    // Devolve a altura total ocupada.
    private double distribuir(double larguraUtil, boolean aplicar) {
        List<Node> itens = itens();
        if (itens.isEmpty()) {
            return 0;
        }
        int colunas = totalColunas(larguraUtil, itens.size());
        double larguraColuna = (larguraUtil - espaco * (colunas - 1)) / colunas;

        List<List<Node>> linhas = new ArrayList<>();
        List<Integer> usadas = new ArrayList<>();
        for (Node item : itens) {
            int ocupa = Math.min(colunasDe(item), colunas);
            int destino = -1;
            for (int i = 0; i < linhas.size(); i++) {
                if (usadas.get(i) + ocupa <= colunas) {
                    destino = i;
                    break;
                }
            }
            if (destino < 0) {
                linhas.add(new ArrayList<>());
                usadas.add(0);
                destino = linhas.size() - 1;
            }
            linhas.get(destino).add(item);
            usadas.set(destino, usadas.get(destino) + ocupa);
        }

        double y = snappedTopInset();
        for (List<Node> linha : linhas) {
            double alturaLinha = 0;
            for (Node item : linha) {
                alturaLinha = Math.max(alturaLinha, item.prefHeight(larguraDe(item, colunas, larguraColuna)));
            }
            if (aplicar) {
                double x = snappedLeftInset();
                for (Node item : linha) {
                    double larguraItem = larguraDe(item, colunas, larguraColuna);
                    item.resizeRelocate(x, y, larguraItem, alturaLinha);
                    x += larguraItem + espaco;
                }
            }
            y += alturaLinha + espaco;
        }
        return Math.max(0, y - snappedTopInset() - espaco);
    }

    private double larguraDe(Node item, int colunas, double larguraColuna) {
        int ocupa = Math.min(colunasDe(item), colunas);
        return larguraColuna * ocupa + espaco * (ocupa - 1);
    }
}
