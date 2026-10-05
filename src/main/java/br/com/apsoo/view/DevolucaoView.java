package br.com.apsoo.view;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.apsoo.controller.DevolucaoController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.CondicaoProduto;
import br.com.apsoo.model.FormaPagamento;
import br.com.apsoo.model.ItemVenda;
import br.com.apsoo.model.Venda;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Casos de uso Localizar Venda e Registrar Devolução
public class DevolucaoView extends VBox {

    private static final String POR_NUMERO = "Número da venda";
    private static final String POR_CLIENTE = "CPF/CNPJ do cliente";

    private final ComboBox<String> cbCriterio = new ComboBox<>();
    private final TextField txtBusca = new TextField();
    private final TableView<Venda> tabelaVendas = new TableView<>();
    private final TableView<ItemVenda> tabelaItens = new TableView<>();
    private final ObservableList<Venda> vendas = FXCollections.observableArrayList();
    private final ObservableList<ItemVenda> itens = FXCollections.observableArrayList();
    private final ComboBox<CondicaoProduto> cbCondicao = new ComboBox<>();
    private final ComboBox<FormaPagamento> cbReembolso = new ComboBox<>();
    private final TextField txtMotivo = new TextField();
    private final Label lblReembolso = new Label();

    private final DevolucaoController controller = new DevolucaoController();
    private final Map<ItemVenda, Integer> quantidades = new LinkedHashMap<>();
    private Venda vendaSelecionada;

    public DevolucaoView() {
        setSpacing(16);
        getChildren().addAll(montarBusca(), montarTabelaVendas(), montarTabelaItens(), montarDevolucao());
        VBox.setVgrow(tabelaItens, Priority.ALWAYS);

        tabelaVendas.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, nova) -> mostrarItens(nova));
        atualizarReembolso();
    }

    private VBox montarBusca() {
        cbCriterio.getItems().addAll(POR_NUMERO, POR_CLIENTE);
        cbCriterio.getSelectionModel().selectFirst();
        cbCriterio.valueProperty().addListener((obs, antigo, novo) -> {
            txtBusca.clear();
            txtBusca.setPromptText(novo.equals(POR_NUMERO) ? "Ex.: 12" : "CPF ou CNPJ");
        });
        txtBusca.setPromptText("Ex.: 12");
        txtBusca.setOnAction(e -> buscar());

        Button btnBuscar = Botoes.comIcone("Buscar", "buscar", "accent");
        btnBuscar.setOnAction(e -> buscar());
        HBox linhaBusca = new HBox(8, txtBusca, btnBuscar);
        HBox.setHgrow(txtBusca, Priority.ALWAYS);

        GradeResponsiva grade = new GradeResponsiva(200, 16);
        grade.getChildren().addAll(new Campo("Localizar por", cbCriterio), new Campo("Informe o valor", linhaBusca));

        VBox card = new VBox(grade);
        card.getStyleClass().add("card");
        return card;
    }

    private TableView<Venda> montarTabelaVendas() {
        tabelaVendas.getColumns().addAll(
                Celulas.<Venda>selo("Nº", 90, v -> "#" + v.getId(), v -> "neutral"),
                Tabelas.<Venda>coluna("Data/hora", 150, v -> Tema.dataHora(v.getDataHora())),
                Celulas.<Venda>destaque("Total", 120, v -> Tema.moeda(v.calcularTotal())),
                Celulas.<Venda>selo("Pagamento", 130, v -> v.getPagamento().getFormaPagamento().toString(),
                        v -> switch (v.getPagamento().getFormaPagamento()) {
                            case DINHEIRO -> "success";
                            case PIX -> "neutral";
                            case CARTAO -> "warning";
                        }),
                Celulas.<Venda>pessoa("Vendedor", 200, v -> v.getUsuario().getNome(), null));
        tabelaVendas.setItems(vendas);
        tabelaVendas.setPlaceholder(Tabelas.vazio("Nenhuma venda localizada"));
        Tabelas.preparar(tabelaVendas);
        tabelaVendas.setMinHeight(120);
        tabelaVendas.setPrefHeight(150);
        return tabelaVendas;
    }

    private TableView<ItemVenda> montarTabelaItens() {
        TableColumn<ItemVenda, Void> colDevolver = new TableColumn<>("Devolver");
        colDevolver.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                setGraphic(empty ? null : criarPasso(getTableView().getItems().get(getIndex())));
            }
        });
        Tabelas.fixa(colDevolver, 200);

        tabelaItens.getColumns().addAll(
                Celulas.<ItemVenda>comIcone("Produto", 230, i -> "produtos", i -> "",
                        i -> i.getProduto().getNome(), null),
                Celulas.<ItemVenda>chip("Categoria", 170, i -> i.getProduto().getCategoria().getNome()),
                Celulas.<ItemVenda>numerica("Vendida", 90, i -> String.valueOf(i.getQuantidade())),
                Celulas.<ItemVenda>numerica("Devolvida", 100, i -> String.valueOf(i.getQuantidadeDevolvida())),
                Tabelas.<ItemVenda>coluna("Prazo até", 190, this::descricaoPrazo),
                colDevolver);
        tabelaItens.setItems(itens);
        tabelaItens.setPlaceholder(Tabelas.vazio("Selecione uma venda para ver os itens"));
        Tabelas.preparar(tabelaItens);
        tabelaItens.setMinHeight(170);
        return tabelaItens;
    }

    private VBox montarDevolucao() {
        cbCondicao.getItems().addAll(CondicaoProduto.values());
        cbCondicao.setPromptText("Selecione");
        cbReembolso.getItems().addAll(FormaPagamento.values());
        cbReembolso.setPromptText("Selecione");
        txtMotivo.setPromptText("Descreva o motivo da devolução");

        Campo campoMotivo = new Campo("Motivo *", txtMotivo);
        GradeResponsiva.ocupar(campoMotivo, 2);
        GradeResponsiva grade = new GradeResponsiva(220, 16);
        grade.getChildren().addAll(new Campo("Condição do produto *", cbCondicao),
                new Campo("Tipo de reembolso *", cbReembolso), campoMotivo);

        lblReembolso.getStyleClass().add("total");
        Button btnRegistrar = Botoes.comIcone("Registrar devolução", "devolucao", "success");
        btnRegistrar.setOnAction(e -> registrarDevolucao());

        VBox card = new VBox(16, grade, new Barra().aEsquerda(lblReembolso).aDireita(btnRegistrar));
        card.getStyleClass().add("card");
        return card;
    }

    private Passo criarPasso(ItemVenda item) {
        boolean permitido = item.quantidadeDevolvivel() > 0
                && vendaSelecionada.isDentroDoPrazo(item, LocalDateTime.now());
        int maximo = permitido ? item.quantidadeDevolvivel() : 0;

        Passo passo = new Passo(0, maximo, Math.min(quantidades.getOrDefault(item, 0), maximo));
        passo.setDisable(!permitido);
        passo.valorProperty().addListener((obs, antigo, novo) -> {
            quantidades.put(item, novo.intValue());
            atualizarReembolso();
        });
        return passo;
    }

    private String descricaoPrazo(ItemVenda item) {
        LocalDateTime limite = vendaSelecionada.prazoLimiteDevolucao(item);
        String texto = Tema.dataHora(limite);
        return LocalDateTime.now().isAfter(limite) ? texto + " (expirado)" : texto;
    }

    private void buscar() {
        try {
            List<Venda> encontradas;
            if (cbCriterio.getValue().equals(POR_NUMERO)) {
                encontradas = List.of(controller.localizarPorNumero(txtBusca.getText()));
            } else {
                encontradas = controller.localizarPorCliente(txtBusca.getText());
            }
            vendas.setAll(encontradas);
            tabelaVendas.getSelectionModel().selectFirst();
        } catch (RegraNegocioException ex) {
            vendas.clear();
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void mostrarItens(Venda venda) {
        vendaSelecionada = venda;
        quantidades.clear();
        itens.clear();
        if (venda != null) {
            itens.setAll(venda.getItens());
        }
        atualizarReembolso();
    }

    private void atualizarReembolso() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<ItemVenda, Integer> entrada : quantidades.entrySet()) {
            total = total.add(entrada.getKey().getPrecoUnitario().multiply(BigDecimal.valueOf(entrada.getValue())));
        }
        lblReembolso.setText("Reembolso: " + Tema.moeda(total));
    }

    private void registrarDevolucao() {
        try {
            int numero = controller.registrarDevolucao(vendaSelecionada, quantidades,
                    cbCondicao.getValue(), cbReembolso.getValue(), txtMotivo.getText());
            Dialogos.informar("Devolução nº " + numero + " registrada com sucesso.\n" + lblReembolso.getText());

            Venda atualizada = controller.recarregar(vendaSelecionada);
            int posicao = vendas.indexOf(vendaSelecionada);
            vendas.set(posicao, atualizada);
            tabelaVendas.getSelectionModel().select(posicao);
            mostrarItens(atualizada);

            cbCondicao.setValue(null);
            cbReembolso.setValue(null);
            txtMotivo.clear();
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
