package br.com.apsoo.view;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Atalhos para montar tabelas somente leitura e responsivas
public final class Tabelas {

    private static final double ALTURA_MINIMA = 220;

    private Tabelas() {
    }

    // Tabela com um título acima, ocupando a altura disponível
    public static VBox comTitulo(String titulo, TableView<?> tabela) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("subtitle");
        VBox bloco = new VBox(10, lblTitulo, tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return bloco;
    }

    // Estado vazio ilustrado, usado como placeholder das tabelas
    public static Node vazio(String texto) {
        StackPane quadro = new StackPane(Icones.de("buscar", 24));
        quadro.getStyleClass().add("icon-chip");
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("muted");
        VBox caixa = new VBox(12, quadro, rotulo);
        caixa.setAlignment(Pos.CENTER);
        return caixa;
    }

    public static <T> TableColumn<T, String> coluna(String titulo, double largura, Function<T, String> valor) {
        TableColumn<T, String> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(valor.apply(c.getValue())));
        coluna.setPrefWidth(largura);
        return coluna;
    }

    // Coluna de largura fixa (ações e selos), que não encolhe nem estica
    public static void fixa(TableColumn<?, ?> coluna, double largura) {
        coluna.setMinWidth(largura);
        coluna.setPrefWidth(largura);
        coluna.setMaxWidth(largura);
    }

    // Aplica o comportamento responsivo: colunas proporcionais com largura mínima (a tabela rola na
    // horizontal quando a janela é muito estreita) e altura mínima para a página rolar em vez de espremer.
    public static void preparar(TableView<?> tabela) {
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabela.setMinHeight(ALTURA_MINIMA);
        for (TableColumn<?, ?> coluna : tabela.getColumns()) {
            coluna.setSortable(false);
            coluna.setReorderable(false);
            if (coluna.getMaxWidth() > 1000 && coluna.getMinWidth() <= 10) { // só colunas flexíveis sem mínimo próprio
                coluna.setMinWidth(Math.max(72, coluna.getPrefWidth() * 0.75));
            }
        }
    }
}
