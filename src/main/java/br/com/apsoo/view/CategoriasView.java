package br.com.apsoo.view;

import java.sql.SQLException;
import java.util.List;

import br.com.apsoo.controller.CategoriaController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.Categoria;
import br.com.apsoo.model.CategoriaHistorico;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Gestão de categorias (RF05): somente administrador
public class CategoriasView extends VBox {

    private final TableView<Categoria> tabela = new TableView<>();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final CategoriaController controller = new CategoriaController();

    public CategoriasView() {
        setSpacing(16);

        Label dica = new Label("Categorias com produtos vinculados não podem ser excluídas, apenas desativadas.");
        dica.getStyleClass().add("muted");
        dica.setWrapText(true);
        Button btnNova = Botoes.comIcone("Nova categoria", "mais", "accent");
        btnNova.setOnAction(e -> {
            if (CategoriaDialog.exibir(controller, null)) {
                carregar();
            }
        });

        getChildren().addAll(new Barra().aEsquerda(dica).aDireita(btnNova), montarTabela());
        VBox.setVgrow(tabela, Priority.ALWAYS);
        carregar();
    }

    private TableView<Categoria> montarTabela() {
        TableColumn<Categoria, Void> colStatus = new TableColumn<>("Status");
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                boolean ativa = getTableView().getItems().get(getIndex()).isAtiva();
                Label badge = new Label(ativa ? "Ativa" : "Desativada");
                badge.getStyleClass().addAll("badge", ativa ? "success" : "danger");
                setGraphic(badge);
            }
        });
        Tabelas.fixa(colStatus, 130);

        TableColumn<Categoria, Void> colAcoes = new TableColumn<>("");
        colAcoes.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Categoria categoria = getTableView().getItems().get(getIndex());
                setGraphic(new HBox(2,
                        Botoes.acao("editar", "Editar categoria", () -> editar(categoria)),
                        Botoes.acao(categoria.isAtiva() ? "desativar" : "ativar",
                                categoria.isAtiva() ? "Desativar categoria" : "Ativar categoria",
                                () -> alterarStatus(categoria)),
                        Botoes.acao("historico", "Histórico de alterações", () -> mostrarHistorico(categoria)),
                        Botoes.acaoPerigosa("excluir", "Excluir categoria", () -> excluir(categoria))));
            }
        });
        Tabelas.fixa(colAcoes, 170);

        tabela.getColumns().addAll(
                Celulas.<Categoria>chip("Categoria", 220, Categoria::getNome),
                Celulas.<Categoria>comIcone("Prazo de devolução", 190, c -> "historico", c -> "",
                        Categoria::descricaoPrazo, null),
                Celulas.<Categoria>medidor("Produtos", 170, c -> c.getQuantidadeProdutos(),
                        c -> c.getQuantidadeProdutos() + (c.getQuantidadeProdutos() == 1 ? " produto" : " produtos")),
                colStatus, colAcoes);
        tabela.setItems(categorias);
        tabela.setPlaceholder(Tabelas.vazio("Nenhuma categoria cadastrada"));
        Tabelas.preparar(tabela);
        return tabela;
    }

    private void carregar() {
        try {
            categorias.setAll(controller.listar());
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void editar(Categoria categoria) {
        if (CategoriaDialog.exibir(controller, categoria)) {
            carregar();
        }
    }

    private void alterarStatus(Categoria categoria) {
        boolean ativar = !categoria.isAtiva();
        if (!ativar && !Dialogos.confirmar("Desativar a categoria \"" + categoria.getNome()
                + "\"? Ela deixará de ser oferecida em novos produtos.")) {
            return;
        }
        try {
            controller.alterarStatus(categoria, ativar);
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
        carregar();
    }

    private void excluir(Categoria categoria) {
        if (!Dialogos.confirmar("Excluir a categoria \"" + categoria.getNome() + "\"?")) {
            return;
        }
        try {
            controller.excluir(categoria);
            carregar();
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    // Consulta somente leitura das alterações já realizadas
    private void mostrarHistorico(Categoria categoria) {
        try {
            List<CategoriaHistorico> historico = controller.historico(categoria);

            TableView<CategoriaHistorico> tabelaHistorico = new TableView<>(
                    FXCollections.observableArrayList(historico));
            tabelaHistorico.getColumns().addAll(
                    Tabelas.<CategoriaHistorico>coluna("Data/hora", 140, h -> Tema.dataHora(h.getDataAlteracao())),
                    Tabelas.<CategoriaHistorico>coluna("Campo", 140, CategoriaHistorico::getCampo),
                    Tabelas.<CategoriaHistorico>coluna("Anterior", 130, CategoriaHistorico::getValorAnterior),
                    Tabelas.<CategoriaHistorico>coluna("Novo", 130, CategoriaHistorico::getValorNovo),
                    Tabelas.<CategoriaHistorico>coluna("Usuário", 130, CategoriaHistorico::getUsuarioNome));
            tabelaHistorico.setPlaceholder(Tabelas.vazio("Nenhuma alteração registrada"));
            Tabelas.preparar(tabelaHistorico);
            tabelaHistorico.setPrefHeight(320);

            new Janela<Boolean>("Histórico - " + categoria.getNome())
                    .conteudo(tabelaHistorico)
                    .largura(760)
                    .confirmar("Fechar", () -> true)
                    .semCancelar()
                    .exibir();
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
