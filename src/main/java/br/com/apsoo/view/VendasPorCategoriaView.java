package br.com.apsoo.view;

import java.sql.SQLException;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.RelatorioController;
import br.com.apsoo.model.LinhaVendaCategoria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Consultar vendas por categoria (RF16): somente administrador
public class VendasPorCategoriaView extends VBox {

    private final FiltroPeriodo filtro = new FiltroPeriodo(this::atualizar);
    private final TableView<LinhaVendaCategoria> tabela = new TableView<>();
    private final ObservableList<LinhaVendaCategoria> linhas = FXCollections.observableArrayList();
    private final RelatorioController controller = new RelatorioController();

    public VendasPorCategoriaView() {
        setSpacing(16);
        getChildren().addAll(filtro, montarTabela());
        VBox.setVgrow(tabela, Priority.ALWAYS);
        atualizar();
    }

    private TableView<LinhaVendaCategoria> montarTabela() {
        // Evita interpretação equivocada dos dados quando a categoria mudou no período
        TableColumn<LinhaVendaCategoria, Void> colObservacao = new TableColumn<>("Observação");
        colObservacao.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty || !getTableView().getItems().get(getIndex()).isAlteradaNoPeriodo()) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label("Categoria alterada no período");
                badge.getStyleClass().addAll("badge", "danger");
                setGraphic(badge);
            }
        });
        colObservacao.setPrefWidth(230);

        tabela.getColumns().addAll(
                Celulas.<LinhaVendaCategoria>chip("Categoria", 190, LinhaVendaCategoria::getCategoriaNome),
                Celulas.<LinhaVendaCategoria>numerica("Qtd. vendida", 110, l -> String.valueOf(l.getQuantidade())),
                Celulas.<LinhaVendaCategoria>medidor("Valor total", 190, l -> l.getValor().doubleValue(),
                        l -> Tema.moeda(l.getValor()), l -> "success"),
                Tabelas.<LinhaVendaCategoria>coluna("Mais vendido", 200,
                        l -> l.getMaisVendido() == null ? "-" : l.getMaisVendido().toString()),
                Tabelas.<LinhaVendaCategoria>coluna("Menos vendido", 200,
                        l -> l.getMenosVendido() == null ? "-" : l.getMenosVendido().toString()),
                colObservacao);
        tabela.setItems(linhas);
        tabela.setPlaceholder(Tabelas.vazio("Nenhuma categoria cadastrada"));
        Tabelas.preparar(tabela);
        return tabela;
    }

    public void atualizar() {
        try {
            linhas.setAll(controller.consultarVendasPorCategoria(filtro.getPeriodo(), filtro.getReferencia()));
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
