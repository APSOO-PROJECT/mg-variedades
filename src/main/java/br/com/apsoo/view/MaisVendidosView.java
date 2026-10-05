package br.com.apsoo.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.RelatorioController;
import br.com.apsoo.model.RelatorioExportavel;
import br.com.apsoo.model.RelatorioProdutosVendidos;
import br.com.apsoo.model.VendaProduto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

// Relatório de produtos mais vendidos e sem venda no período (RF20): somente administrador
public class MaisVendidosView extends VBox {

    private final FiltroPeriodo filtro = new FiltroPeriodo(this::atualizar);
    private final Indicador indMaisVendido = new Indicador("Produto mais vendido do período")
            .comIcone("estrela", "warning");
    private final ObservableList<VendaProduto> vendidos = FXCollections.observableArrayList();
    private final ObservableList<VendaProduto> semVenda = FXCollections.observableArrayList();
    private final RelatorioController controller = new RelatorioController();
    private RelatorioExportavel exportacao; // resultado da última consulta exibida
    private final BotaoExportar btnExportar = new BotaoExportar(() -> exportacao);

    public MaisVendidosView() {
        setSpacing(16);

        TableView<VendaProduto> tabelaVendidos = new TableView<>(vendidos);
        tabelaVendidos.getColumns().addAll(
                Celulas.<VendaProduto>posicao("#"),
                Celulas.<VendaProduto>chip("Categoria", 170, VendaProduto::categoria),
                Celulas.<VendaProduto>comIcone("Produto", 210, v -> "produtos", v -> "", VendaProduto::produto, null),
                Celulas.<VendaProduto>medidor("Qtd. vendida", 150, v -> v.quantidade(),
                        v -> v.quantidade() + " un.", v -> "success"));
        tabelaVendidos.setPlaceholder(Tabelas.vazio("Nenhuma venda no período"));
        Tabelas.preparar(tabelaVendidos);
        tabelaVendidos.setPrefHeight(360);

        TableView<VendaProduto> tabelaSemVenda = new TableView<>(semVenda);
        tabelaSemVenda.getColumns().addAll(
                Celulas.<VendaProduto>chip("Categoria", 160, VendaProduto::categoria),
                Celulas.<VendaProduto>comIcone("Produto", 210, v -> "alerta", v -> "warning", VendaProduto::produto,
                        v -> "Nenhuma venda no período"));
        tabelaSemVenda.setPlaceholder(Tabelas.vazio("Todos os produtos tiveram venda"));
        Tabelas.preparar(tabelaSemVenda);
        tabelaSemVenda.setPrefHeight(360);

        GradeResponsiva tabelas = new GradeResponsiva(360, 16);
        tabelas.getChildren().addAll(Tabelas.comTitulo("Ranking de mais vendidos", tabelaVendidos),
                Tabelas.comTitulo("Sem nenhuma venda no período", tabelaSemVenda));

        GradeResponsiva indicadores = new GradeResponsiva(260, 16);
        indicadores.getChildren().add(indMaisVendido);

        getChildren().addAll(btnExportar.barraCom(filtro), indicadores, tabelas);
        atualizar();
    }

    private RelatorioExportavel montarExportacao(RelatorioProdutosVendidos relatorio) {
        RelatorioExportavel saida = new RelatorioExportavel("Relatório de produtos mais vendidos");
        saida.adicionarFiltro("Período", filtro.getPeriodo().descrever(filtro.getReferencia()));
        saida.adicionarResumo("Produto mais vendido do período", relatorio.getProdutoMaisVendido() == null
                ? "-" : relatorio.getProdutoMaisVendido().toString());

        List<List<String>> maisVendidos = relatorio.getMaisVendidosPorCategoria().stream()
                .map(v -> List.of(v.categoria(), v.produto(), String.valueOf(v.quantidade())))
                .toList();
        saida.adicionarSecao("Mais vendidos por categoria", List.of("Categoria", "Produto", "Qtd. vendida"),
                maisVendidos);

        List<List<String>> semVendas = relatorio.getSemVenda().stream()
                .map(v -> List.of(v.categoria(), v.produto()))
                .toList();
        saida.adicionarSecao("Sem nenhuma venda no período", List.of("Categoria", "Produto"), semVendas);
        return saida;
    }

    public final void atualizar() {
        try {
            RelatorioProdutosVendidos relatorio =
                    controller.consultarProdutosVendidos(filtro.getPeriodo(), filtro.getReferencia());
            indMaisVendido.setValor(relatorio.getProdutoMaisVendido() == null
                    ? "-" : relatorio.getProdutoMaisVendido().toString());
            // Na tela, o ranking vai do mais para o menos vendido (a categoria aparece em cada linha);
            // o relatório exportado mantém o agrupamento por categoria
            List<VendaProduto> ranking = new ArrayList<>(relatorio.getMaisVendidosPorCategoria());
            ranking.sort(Comparator.comparingInt(VendaProduto::quantidade).reversed()
                    .thenComparing(VendaProduto::produto));
            vendidos.setAll(ranking);
            semVenda.setAll(relatorio.getSemVenda());
            exportacao = montarExportacao(relatorio);
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
