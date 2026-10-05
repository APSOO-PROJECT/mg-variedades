package br.com.apsoo.view;

import java.math.BigDecimal;

import br.com.apsoo.controller.CaixaController;
import br.com.apsoo.model.Caixa;
import br.com.apsoo.model.ResumoCaixa;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

// Fechamento: compara o valor esperado com o contado e exige justificativa em caso de quebra
public final class FechamentoDialog {

    private FechamentoDialog() {
    }

    // Retorna o caixa fechado ou null se o administrador cancelar
    public static Caixa exibir(CaixaController controller, ResumoCaixa resumo) {
        BigDecimal esperado = resumo.calcularSaldoEsperado();

        Label lblEsperado = new Label("Valor esperado em caixa: " + Tema.moeda(esperado));
        lblEsperado.getStyleClass().add("subtitle");

        TextField txtContado = new TextField();
        txtContado.setPromptText("0,00");
        Label lblDiferenca = new Label();
        lblDiferenca.getStyleClass().add("muted");
        TextField txtJustificativa = new TextField();
        txtJustificativa.setPromptText("Justificativa da quebra de caixa");
        txtJustificativa.setDisable(true);

        txtContado.textProperty().addListener((obs, antigo, novo) -> {
            try {
                BigDecimal diferenca = Tema.lerValor(novo).subtract(esperado);
                boolean quebra = diferenca.signum() != 0;
                lblDiferenca.setText(quebra ? "Quebra de caixa: " + Tema.moeda(diferenca) : "Sem divergência");
                txtJustificativa.setDisable(!quebra);
            } catch (NumberFormatException ex) {
                lblDiferenca.setText("");
                txtJustificativa.setDisable(true);
            }
        });

        VBox conteudo = new VBox(14, lblEsperado,
                new Campo("Valor contado (R$) *", txtContado),
                lblDiferenca,
                new Campo("Justificativa", txtJustificativa).comDica("Obrigatória quando há quebra de caixa."));

        return new Janela<Caixa>("Fechar caixa")
                .conteudo(conteudo)
                .largura(460)
                .confirmar("Fechar caixa", () -> {
                    BigDecimal contado;
                    try {
                        contado = Tema.lerValor(txtContado.getText());
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("Informe o valor contado.");
                    }
                    return controller.fecharCaixa(contado, txtJustificativa.getText());
                })
                .exibir();
    }
}
