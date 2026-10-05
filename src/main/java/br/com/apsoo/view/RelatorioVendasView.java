package br.com.apsoo.view;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.RelatorioController;
import br.com.apsoo.model.FormaPagamento;
import br.com.apsoo.model.LinhaVendaCategoria;
import br.com.apsoo.model.RelatorioExportavel;
import br.com.apsoo.model.RelatorioVendas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

// Relatório de vendas por período (RF15): somente administrador
public class RelatorioVendasView extends VBox {

    private final FiltroPeriodo filtro = new FiltroPeriodo(this::atualizar);
    private final Indicador indTotal = new Indicador("Valor total das vendas").comIcone("caixa", "success");
    private final Indicador indQuantidade = new Indicador("Número de vendas").comIcone("vendas", "");
    private final Indicador indMedia = new Indicador("Média por venda").comIcone("tendencia", "");
    private final Indicador indMaisVendido = new Indicador("Produto mais vendido").comIcone("estrela", "warning");
    private final ObservableList<LinhaVendaCategoria> categorias = FXCollections.observableArrayList();
    private final ObservableList<Map.Entry<FormaPagamento, BigDecimal>> formas = FXCollections.observableArrayList();
    private final RelatorioController controller = new RelatorioController();
    private RelatorioExportavel exportacao; // resultado da última consulta exibida
    private final BotaoExportar btnExportar = new BotaoExportar(() -> exportacao);

    public RelatorioVendasView() {
        setSpacing(16);

        GradeResponsiva indicadores = new GradeResponsiva(220, 16);
        indicadores.getChildren().addAll(indTotal, indQuantidade, indMedia, indMaisVendido);

        VBox blocoCategorias = Tabelas.comTitulo("Vendas por categoria", montarTabelaCategorias());
        VBox blocoFormas = Tabelas.comTitulo("Vendas por forma de pagamento", montarTabelaFormas());
        GradeResponsiva.ocupar(blocoCategorias, 2);
        GradeResponsiva tabelas = new GradeResponsiva(340, 16);
        tabelas.getChildren().addAll(blocoCategorias, blocoFormas);

        getChildren().addAll(btnExportar.barraCom(filtro), indicadores, tabelas);
        atualizar();
    }

    private TableView<LinhaVendaCategoria> montarTabelaCategorias() {
        TableView<LinhaVendaCategoria> tabela = new TableView<>(categorias);

        // Nota visual quando a categoria foi alterada no período consultado
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
                Celulas.<LinhaVendaCategoria>chip("Categoria", 180, LinhaVendaCategoria::getCategoriaNome),
                Celulas.<LinhaVendaCategoria>numerica("Qtd.", 70, l -> String.valueOf(l.getQuantidade())),
                Celulas.<LinhaVendaCategoria>medidor("Valor", 170, l -> l.getValor().doubleValue(),
                        l -> Tema.moeda(l.getValor()), l -> "success"),
                colObservacao);
        tabela.setPlaceholder(Tabelas.vazio("Nenhuma categoria cadastrada"));
        Tabelas.preparar(tabela);
        tabela.setPrefHeight(360);
        return tabela;
    }

    private TableView<Map.Entry<FormaPagamento, BigDecimal>> montarTabelaFormas() {
        TableView<Map.Entry<FormaPagamento, BigDecimal>> tabela = new TableView<>(formas);
        tabela.getColumns().addAll(
                Celulas.<Map.Entry<FormaPagamento, BigDecimal>>comIcone("Forma", 150,
                        e -> switch (e.getKey()) {
                            case DINHEIRO -> "caixa";
                            case PIX -> "confirmar";
                            case CARTAO -> "cartao";
                        },
                        e -> switch (e.getKey()) {
                            case DINHEIRO -> "success";
                            case PIX -> "";
                            case CARTAO -> "warning";
                        },
                        e -> e.getKey().toString(), null),
                Celulas.<Map.Entry<FormaPagamento, BigDecimal>>medidor("Valor", 150,
                        e -> e.getValue().doubleValue(), e -> Tema.moeda(e.getValue())));
        Tabelas.preparar(tabela);
        tabela.setPrefHeight(360);
        return tabela;
    }

    private RelatorioExportavel montarExportacao(RelatorioVendas relatorio) {
        RelatorioExportavel saida = new RelatorioExportavel("Relatório de vendas por período");
        saida.adicionarFiltro("Período", filtro.getPeriodo().descrever(filtro.getReferencia()));
        saida.adicionarResumo("Valor total das vendas", Tema.moeda(relatorio.getValorTotal()));
        saida.adicionarResumo("Número de vendas", String.valueOf(relatorio.getQuantidadeVendas()));
        saida.adicionarResumo("Média por venda", Tema.moeda(relatorio.calcularMedia()));
        saida.adicionarResumo("Produto mais vendido", relatorio.getProdutoMaisVendido() == null
                ? "-" : relatorio.getProdutoMaisVendido().toString());

        List<List<String>> porCategoria = relatorio.getCategorias().stream()
                .map(l -> List.of(l.getCategoriaNome(), String.valueOf(l.getQuantidade()), Tema.moeda(l.getValor()),
                        l.isAlteradaNoPeriodo() ? "Categoria alterada no período" : ""))
                .toList();
        saida.adicionarSecao("Vendas por categoria", List.of("Categoria", "Qtd.", "Valor", "Observação"), porCategoria);

        List<List<String>> porForma = relatorio.getVendasPorForma().entrySet().stream()
                .map(e -> List.of(e.getKey().toString(), Tema.moeda(e.getValue())))
                .toList();
        saida.adicionarSecao("Vendas por forma de pagamento", List.of("Forma", "Valor"), porForma);
        return saida;
    }

    public final void atualizar() {
        try {
            RelatorioVendas relatorio = controller.consultarVendas(filtro.getPeriodo(), filtro.getReferencia());
            indTotal.setValor(Tema.moeda(relatorio.getValorTotal()));
            indQuantidade.setValor(String.valueOf(relatorio.getQuantidadeVendas()));
            indMedia.setValor(Tema.moeda(relatorio.calcularMedia()));
            indMaisVendido.setValor(relatorio.getProdutoMaisVendido() == null
                    ? "-" : relatorio.getProdutoMaisVendido().toString());
            categorias.setAll(relatorio.getCategorias());
            formas.setAll(relatorio.getVendasPorForma().entrySet());
            exportacao = montarExportacao(relatorio);
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
