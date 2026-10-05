package br.com.apsoo.view;

import java.sql.SQLException;

import br.com.apsoo.controller.ProdutoController;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.StatusProduto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Consulta rápida de estoque (RF14): nome, categoria, quantidade atual e mínima
public class EstoqueView extends VBox {

    private final TextField txtBusca = new TextField();
    private final CheckBox chkEstoqueBaixo = new CheckBox("Somente estoque baixo");
    private final TableView<Produto> tabela = new TableView<>();
    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private final FilteredList<Produto> filtrados = new FilteredList<>(produtos);
    private final ProdutoController controller = new ProdutoController();

    public EstoqueView() {
        setSpacing(16);

        txtBusca.setPromptText("Buscar por nome, categoria ou código de barras");
        txtBusca.setPrefWidth(380);
        Button btnAtualizar = Botoes.comIcone("Atualizar", "atualizar");
        btnAtualizar.setOnAction(e -> carregar());

        getChildren().addAll(new Barra().aEsquerda(txtBusca, chkEstoqueBaixo).aDireita(btnAtualizar), montarTabela());
        VBox.setVgrow(tabela, Priority.ALWAYS);

        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        chkEstoqueBaixo.selectedProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        carregar();
    }

    private TableView<Produto> montarTabela() {
        TableColumn<Produto, Void> colNome = Celulas.comIcone("Produto", 260, p -> "produtos",
                p -> p.sinalizarEstoqueBaixo() ? "danger" : "", Produto::getNome, null);
        TableColumn<Produto, Void> colCategoria = Celulas.chip("Categoria", 180, p -> p.getCategoria().getNome());
        TableColumn<Produto, Void> colAtual = Celulas.medidor("Qtd. atual", 180, p -> p.getQuantidadeAtual(),
                p -> String.valueOf(p.getQuantidadeAtual()), p -> p.sinalizarEstoqueBaixo() ? "danger" : "success");
        TableColumn<Produto, String> colMinima = Celulas.numerica("Qtd. mínima", 130,
                p -> String.valueOf(p.getQuantidadeMinima()));
        TableColumn<Produto, Void> colStatus = Celulas.selo("Status", 140,
                p -> p.getStatus() == StatusProduto.DISPONIVEL ? "Disponível" : "Indisponível",
                p -> p.getStatus() == StatusProduto.DISPONIVEL ? "success" : "neutral");

        TableColumn<Produto, Void> colSituacao = new TableColumn<>("Situação");
        colSituacao.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Produto produto = getTableView().getItems().get(getIndex());
                boolean semEstoque = produto.getQuantidadeAtual() == 0;
                boolean baixo = produto.sinalizarEstoqueBaixo();

                Label badge = new Label(semEstoque ? "Sem estoque" : baixo ? "Estoque baixo" : "Normal");
                badge.getStyleClass().addAll("badge", baixo ? "danger" : "success");
                setGraphic(badge);
            }
        });
        Tabelas.fixa(colSituacao, 150);

        tabela.getColumns().addAll(colNome, colCategoria, colAtual, colMinima, colStatus, colSituacao);
        tabela.setItems(filtrados);
        tabela.setPlaceholder(Tabelas.vazio("Nenhum produto encontrado"));
        Tabelas.preparar(tabela);
        return tabela;
    }

    private void carregar() {
        try {
            produtos.setAll(controller.consultarEstoque());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void aplicarFiltro() {
        String texto = txtBusca.getText().trim().toLowerCase();
        filtrados.setPredicate(p -> (texto.isEmpty()
                || p.getNome().toLowerCase().contains(texto)
                || p.getCategoria().getNome().toLowerCase().contains(texto)
                || (p.getCodigoBarras() != null && p.getCodigoBarras().contains(texto)))
                && (!chkEstoqueBaixo.isSelected() || p.sinalizarEstoqueBaixo()));
    }
}
