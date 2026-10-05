package br.com.apsoo.view;

import java.math.BigDecimal;

import br.com.apsoo.controller.CaixaController;
import br.com.apsoo.model.TipoOperacao;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

// Registro de sangria (retirada) ou suprimento (inclusão) de valores no caixa
public final class MovimentacaoDialog {

    private MovimentacaoDialog() {
    }

    // Retorna true se a movimentação foi registrada
    public static boolean exibir(CaixaController controller, TipoOperacao tipo) {
        TextField txtValor = new TextField();
        txtValor.setPromptText("0,00");
        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText(tipo == TipoOperacao.SANGRIA ? "Ex.: pagamento de fornecedor" : "Ex.: reforço de troco");

        String rotuloValor = tipo == TipoOperacao.SANGRIA ? "Valor retirado (R$) *" : "Valor suprido (R$) *";
        VBox conteudo = new VBox(14, new Campo(rotuloValor, txtValor), new Campo("Motivo *", txtMotivo));

        Boolean registrada = new Janela<Boolean>(tipo.toString())
                .conteudo(conteudo)
                .largura(420)
                .confirmar("Registrar", () -> {
                    BigDecimal valor;
                    try {
                        valor = Tema.lerValor(txtValor.getText());
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("Informe um valor válido.");
                    }
                    if (tipo == TipoOperacao.SANGRIA) {
                        controller.registrarSangria(valor, txtMotivo.getText());
                    } else {
                        controller.registrarSuprimento(valor, txtMotivo.getText());
                    }
                    return true;
                })
                .exibir();
        return registrada != null && registrada;
    }
}
