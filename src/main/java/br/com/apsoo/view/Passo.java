package br.com.apsoo.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

// Seletor numérico com botões - e + (substitui o Spinner)
public class Passo extends HBox {

    private final IntegerProperty valor = new SimpleIntegerProperty();
    private final TextField campo = new TextField();
    private int minimo;
    private int maximo;

    public Passo(int minimo, int maximo, int inicial) {
        super(8);
        this.minimo = minimo;
        this.maximo = maximo;
        setAlignment(Pos.CENTER_LEFT);

        Button btnMenos = botao("menos", "Diminuir");
        btnMenos.setOnAction(e -> definir(valor.get() - 1));
        Button btnMais = botao("mais", "Aumentar");
        btnMais.setOnAction(e -> definir(valor.get() + 1));

        campo.getStyleClass().add("passo");
        campo.setPrefColumnCount(3);
        campo.setMinWidth(56);
        campo.setPrefWidth(72);
        campo.focusedProperty().addListener((obs, antigo, focado) -> {
            if (!focado) {
                aplicarTexto();
            }
        });
        campo.setOnAction(e -> aplicarTexto());
        campo.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) {
                campo.setText(antigo);
            }
        });
        valor.addListener((obs, antigo, novo) -> campo.setText(String.valueOf(novo)));

        HBox.setHgrow(campo, Priority.SOMETIMES);
        getChildren().addAll(btnMenos, campo, btnMais);
        definir(inicial);
    }

    private Button botao(String icone, String dica) {
        Button botao = new Button();
        botao.setGraphic(Icones.de(icone, 16));
        botao.getStyleClass().add("icone-btn");
        botao.setTooltip(new Tooltip(dica));
        botao.setFocusTraversable(false);
        return botao;
    }

    private void aplicarTexto() {
        String texto = campo.getText();
        definir(texto.isBlank() ? minimo : texto.length() > 9 ? maximo : Integer.parseInt(texto));
        campo.setText(String.valueOf(valor.get()));
    }

    private void definir(int novo) {
        valor.set(Math.max(minimo, Math.min(maximo, novo)));
        campo.setText(String.valueOf(valor.get()));
    }

    // Valor digitado, mesmo que o campo ainda esteja em edição
    public int getValor() {
        aplicarTexto();
        return valor.get();
    }

    public void setValor(int novo) {
        definir(novo);
    }

    public void setMaximo(int maximo) {
        this.maximo = maximo;
        definir(valor.get());
    }

    public IntegerProperty valorProperty() {
        return valor;
    }

    @Override
    public void requestFocus() {
        campo.requestFocus();
    }
}
