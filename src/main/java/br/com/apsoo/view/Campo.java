package br.com.apsoo.view;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// Campo de formulário: rótulo acima do controle (funciona em qualquer largura) e dica opcional
public class Campo extends VBox {

    public Campo(String rotulo, Node controle) {
        super(6);
        Label lblRotulo = new Label(rotulo);
        lblRotulo.getStyleClass().add("field-label");
        getChildren().addAll(lblRotulo, controle);

        if (controle instanceof Control control) {
            control.setMaxWidth(Double.MAX_VALUE);
        } else if (controle instanceof Region regiao) {
            regiao.setMaxWidth(Double.MAX_VALUE);
        }
        setMinWidth(0);
    }

    public Campo comDica(String dica) {
        Label lblDica = new Label(dica);
        lblDica.getStyleClass().add("hint");
        lblDica.setWrapText(true);
        getChildren().add(lblDica);
        return this;
    }
}
