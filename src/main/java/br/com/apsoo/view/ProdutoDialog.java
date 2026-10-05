package br.com.apsoo.view;

import java.util.List;

import br.com.apsoo.controller.ProdutoController;
import br.com.apsoo.model.Categoria;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.StatusProduto;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

// Formulário de inclusão e alteração de produto
public final class ProdutoDialog {

    private ProdutoDialog() {
    }

    // Retorna true se o produto foi salvo
    public static boolean exibir(ProdutoController controller, Produto existente, List<Categoria> categorias) {
        TextField txtNome = new TextField();
        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Opcional");
        ComboBox<Categoria> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().addAll(categorias);
        cbCategoria.setPromptText("Selecione");
        TextField txtCusto = new TextField();
        TextField txtPreco = new TextField();
        TextField txtAtual = new TextField("0");
        TextField txtMinima = new TextField("0");
        ComboBox<StatusProduto> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll(StatusProduto.values());
        cbStatus.setValue(StatusProduto.DISPONIVEL);

        if (existente != null) {
            txtNome.setText(existente.getNome());
            txtCodigo.setText(existente.getCodigoBarras() == null ? "" : existente.getCodigoBarras());
            cbCategoria.getSelectionModel().select(categorias.stream()
                    .filter(c -> c.getId().equals(existente.getCategoria().getId())).findFirst().orElse(null));
            txtCusto.setText(existente.getCustoAquisicao().toPlainString());
            txtPreco.setText(existente.getPreco().toPlainString());
            txtAtual.setText(String.valueOf(existente.getQuantidadeAtual()));
            txtMinima.setText(String.valueOf(existente.getQuantidadeMinima()));
            cbStatus.setValue(existente.getStatus());
        }

        Campo campoNome = new Campo("Nome *", txtNome);
        GradeResponsiva.ocupar(campoNome, 2);
        GradeResponsiva grade = new GradeResponsiva(210, 14);
        grade.getChildren().addAll(campoNome,
                new Campo("Código de barras", txtCodigo),
                new Campo("Categoria *", cbCategoria),
                new Campo("Custo de aquisição (R$) *", txtCusto),
                new Campo("Preço de venda (R$) *", txtPreco),
                new Campo("Quantidade atual *", txtAtual),
                new Campo("Quantidade mínima *", txtMinima),
                new Campo("Status *", cbStatus));

        VBox conteudo = new VBox(14, grade);
        if (existente != null) {
            Label auditoria = new Label("Adicionado em " + Tema.dataHora(existente.getDataAdicao())
                    + "\nÚltima atualização: " + Tema.dataHora(existente.getDataAtualizacao())
                    + (existente.getUsuarioAtualizacaoNome() == null ? "" : " por " + existente.getUsuarioAtualizacaoNome()));
            auditoria.getStyleClass().addAll("muted", "hint");
            conteudo.getChildren().add(auditoria);
        }

        Boolean salvo = new Janela<Boolean>(existente == null ? "Novo produto" : "Alterar produto")
                .conteudo(conteudo)
                .largura(580)
                .confirmar("Salvar", () -> {
                    Produto produto = new Produto();
                    produto.setId(existente == null ? null : existente.getId());
                    produto.setNome(txtNome.getText().trim());
                    produto.setCodigoBarras(txtCodigo.getText().isBlank() ? null : txtCodigo.getText().trim());
                    produto.setCategoria(cbCategoria.getValue());
                    try {
                        produto.setCustoAquisicao(Tema.lerValor(txtCusto.getText()));
                        produto.setPreco(Tema.lerValor(txtPreco.getText()));
                        produto.setQuantidadeAtual(Integer.parseInt(txtAtual.getText().trim()));
                        produto.setQuantidadeMinima(Integer.parseInt(txtMinima.getText().trim()));
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("Preencha os valores e as quantidades com números válidos.");
                    }
                    produto.setStatus(cbStatus.getValue());
                    controller.salvar(produto);
                    return true;
                })
                .exibir();
        return salvo != null && salvo;
    }
}
