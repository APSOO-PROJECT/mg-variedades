package br.com.apsoo.view;

import java.time.LocalDate;

import br.com.apsoo.controller.Periodo;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

// Filtro por dia, mês ou ano a partir de uma data de referência (RF13, RF15, RF16); quebra linha em janelas estreitas
public class FiltroPeriodo extends Fluxo {

    private final ComboBox<Periodo> cbPeriodo = new ComboBox<>();
    private final DatePicker dpReferencia = new DatePicker(LocalDate.now());

    public FiltroPeriodo(Runnable aoMudar) {
        super(16, 8);
        setAlignment(Pos.CENTER_LEFT);

        cbPeriodo.getItems().addAll(Periodo.values());
        cbPeriodo.setValue(Periodo.DIA);
        cbPeriodo.setPrefWidth(120);
        cbPeriodo.valueProperty().addListener((obs, antigo, novo) -> aoMudar.run());

        dpReferencia.setPrefWidth(170);
        dpReferencia.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                aoMudar.run();
            }
        });

        getChildren().addAll(rotulado("Período", cbPeriodo), rotulado("Referência", dpReferencia));
    }

    private HBox rotulado(String texto, Node controle) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("field-label");
        HBox grupo = new HBox(8, rotulo, controle);
        grupo.setAlignment(Pos.CENTER_LEFT);
        return grupo;
    }

    public Periodo getPeriodo() {
        return cbPeriodo.getValue();
    }

    // Data digitada incompleta ou apagada volta para hoje
    public LocalDate getReferencia() {
        return dpReferencia.getValue() == null ? LocalDate.now() : dpReferencia.getValue();
    }
}
