package br.com.apsoo.view;

import br.com.apsoo.controller.CategoriaController;
import br.com.apsoo.model.Categoria;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Formulário de cadastro e edição de categoria
public final class CategoriaDialog {

    private static final String DIAS = "dias";
    private static final String HORAS = "horas";

    private CategoriaDialog() {
    }

    // Retorna true se a categoria foi salva
    public static boolean exibir(CategoriaController controller, Categoria existente) {
        TextField txtNome = new TextField();
        TextField txtPrazo = new TextField();
        txtPrazo.setPromptText("Padrão: 7 dias");
        ComboBox<String> cbUnidade = new ComboBox<>();
        cbUnidade.getItems().addAll(DIAS, HORAS);
        cbUnidade.setValue(DIAS);
        cbUnidade.setPrefWidth(110);

        if (existente != null) {
            txtNome.setText(existente.getNome());
            int horas = existente.getPrazoDevolucaoHoras();
            if (horas % 24 == 0) {
                txtPrazo.setText(String.valueOf(horas / 24));
            } else {
                txtPrazo.setText(String.valueOf(horas));
                cbUnidade.setValue(HORAS);
            }
        }

        HBox linhaPrazo = new HBox(8, txtPrazo, cbUnidade);
        HBox.setHgrow(txtPrazo, Priority.ALWAYS);

        VBox conteudo = new VBox(14, new Campo("Nome *", txtNome),
                new Campo("Prazo de devolução", linhaPrazo)
                        .comDica("Tempo máximo, após a venda, para aceitar a devolução de produtos desta categoria."));

        Boolean salva = new Janela<Boolean>(existente == null ? "Nova categoria" : "Editar categoria")
                .conteudo(conteudo)
                .largura(460)
                .confirmar("Salvar", () -> {
                    Categoria categoria = existente == null ? new Categoria() : existente;
                    categoria.setNome(txtNome.getText().trim());
                    categoria.setPrazoDevolucaoHoras(lerPrazoEmHoras(txtPrazo.getText(), cbUnidade.getValue()));
                    controller.salvar(categoria);
                    return true;
                })
                .exibir();
        return salva != null && salva;
    }

    // Campo vazio vale 0, e o controller aplica o prazo padrão de 7 dias
    private static int lerPrazoEmHoras(String texto, String unidade) {
        if (texto.isBlank()) {
            return 0;
        }
        int valor;
        try {
            valor = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe o prazo com um número inteiro maior que zero.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("Informe o prazo com um número inteiro maior que zero.");
        }
        return unidade.equals(DIAS) ? valor * 24 : valor;
    }
}
