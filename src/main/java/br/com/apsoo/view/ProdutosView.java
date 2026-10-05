package br.com.apsoo.view;

import java.sql.SQLException;
import java.util.List;

import br.com.apsoo.controller.ProdutoController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.Categoria;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Gestão de produtos (RF05): somente administrador
public class ProdutosView extends VBox {

    private final TextField txtFiltro = new TextField();
    private final CheckBox chkEstoqueBaixo = new CheckBox("Somente estoque baixo");
    private final Label lblAlerta = new Label();
    private final TableView<Produto> tabela = new TableView<>();
    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private final FilteredList<Produto> filtrados = new FilteredList<>(produtos);
    private final ProdutoController controller = new ProdutoController();

    public ProdutosView() {
        setSpacing(16);

        txtFiltro.setPromptText("Filtrar por nome, categoria ou código de barras");
        txtFiltro.setPrefWidth(340);
        lblAlerta.getStyleClass().addAll("badge", "danger");
        Button btnNovo = Botoes.comIcone("Novo produto", "mais", "accent");
        btnNovo.setOnAction(e -> abrirFormulario(null));

        getChildren().addAll(new Barra().aEsquerda(txtFiltro, chkEstoqueBaixo, lblAlerta).aDireita(btnNovo),
                montarTabela());
        VBox.setVgrow(tabela, Priority.ALWAYS);

        txtFiltro.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        chkEstoqueBaixo.selectedProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        carregar();
    }

    private TableView<Produto> montarTabela() {
        TableColumn<Produto, Void> colAcoes = new TableColumn<>("");
        colAcoes.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Produto produto = getTableView().getItems().get(getIndex());
                setGraphic(new HBox(2,
                        Botoes.acao("editar", "Editar produto", () -> abrirFormulario(produto)),
                        Botoes.acaoPerigosa("excluir", "Remover produto", () -> remover(produto))));
            }
        });
        Tabelas.fixa(colAcoes, 100);

        tabela.getColumns().addAll(
                Celulas.<Produto>comIcone("Produto", 230, p -> "produtos", p -> p.sinalizarEstoqueBaixo() ? "danger" : "",
                        Produto::getNome, p -> p.getCodigoBarras() == null ? "Sem código" : p.getCodigoBarras()),
                Celulas.<Produto>chip("Categoria", 190, p -> p.getCategoria().getNome()
                        + (p.getCategoria().isAtiva() ? "" : " (desativada)")),
                Celulas.<Produto>numerica("Custo", 100, p -> Tema.moeda(p.getCustoAquisicao())),
                Celulas.<Produto>destaque("Preço", 100, p -> Tema.moeda(p.getPreco())),
                Celulas.<Produto>medidor("Estoque", 160, p -> p.getQuantidadeAtual(),
                        p -> p.getQuantidadeAtual() + " · mín. " + p.getQuantidadeMinima(),
                        p -> p.sinalizarEstoqueBaixo() ? "danger" : "success"),
                // Produtos com a quantidade mínima atingida são sinalizados no status (RF05, regra 1)
                Celulas.<Produto>selo("Status", 140,
                        p -> p.getStatus() == StatusProduto.INDISPONIVEL ? "Indisponível"
                                : p.sinalizarEstoqueBaixo() ? "Estoque baixo" : "Disponível",
                        p -> p.getStatus() == StatusProduto.INDISPONIVEL ? "neutral"
                                : p.sinalizarEstoqueBaixo() ? "danger" : "success"),
                colAcoes);
        tabela.setItems(filtrados);
        tabela.setPlaceholder(Tabelas.vazio("Nenhum produto encontrado"));
        Tabelas.preparar(tabela);
        return tabela;
    }

    private void carregar() {
        try {
            produtos.setAll(controller.listar());
            long baixos = produtos.stream().filter(Produto::sinalizarEstoqueBaixo).count();
            lblAlerta.setText(baixos + (baixos == 1 ? " produto com estoque baixo" : " produtos com estoque baixo"));
            lblAlerta.setVisible(baixos > 0);
            lblAlerta.setManaged(baixos > 0);
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void aplicarFiltro() {
        String texto = txtFiltro.getText().trim().toLowerCase();
        filtrados.setPredicate(p -> (texto.isEmpty()
                || p.getNome().toLowerCase().contains(texto)
                || p.getCategoria().getNome().toLowerCase().contains(texto)
                || (p.getCodigoBarras() != null && p.getCodigoBarras().contains(texto)))
                && (!chkEstoqueBaixo.isSelected() || p.sinalizarEstoqueBaixo()));
    }

    private void abrirFormulario(Produto produto) {
        try {
            List<Categoria> categorias = controller.categoriasDisponiveis(produto);
            if (ProdutoDialog.exibir(controller, produto, categorias)) {
                carregar();
            }
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void remover(Produto produto) {
        if (!Dialogos.confirmar("Remover o produto \"" + produto.getNome() + "\"?")) {
            return;
        }
        try {
            controller.remover(produto);
            carregar();
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
