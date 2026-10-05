package br.com.apsoo.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Cartão de indicador: ícone opcional, título e valor de destaque (painel, caixa e relatórios)
public class Indicador extends HBox {

    private final Label lblValor = new Label();
    private final VBox textos;

    public Indicador(String titulo) {
        super(14);
        setAlignment(Pos.CENTER_LEFT);
        setFillHeight(false); // o ícone mantém o tamanho próprio em vez de esticar com o cartão

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("muted");
        lblTitulo.setWrapText(true);
        lblValor.getStyleClass().add("metric-value");
        lblValor.setWrapText(true);

        textos = new VBox(2, lblTitulo, lblValor);
        getChildren().add(textos);

        getStyleClass().addAll("card", "metric");
        setMinWidth(0);
        setMaxWidth(Double.MAX_VALUE);
    }

    public Indicador(String titulo, String valor) {
        this(titulo);
        setValor(valor);
    }

    // Ícone em um quadro colorido; estilo: "", "success", "danger" ou "warning"
    public Indicador comIcone(String icone, String estilo) {
        StackPane quadro = new StackPane(Icones.de(icone, 22));
        quadro.getStyleClass().add("icon-chip");
        if (!estilo.isEmpty()) {
            quadro.getStyleClass().add(estilo);
        }
        getChildren().add(0, quadro);
        return this;
    }

    // Mantido por compatibilidade com telas ainda não migradas para GradeResponsiva
    public Indicador expandir() {
        HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS);
        return this;
    }

    public void setValor(String valor) {
        lblValor.setText(valor);
    }
}
