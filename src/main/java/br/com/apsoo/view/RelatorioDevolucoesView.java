package br.com.apsoo.view;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.RelatorioController;
import br.com.apsoo.model.CondicaoProduto;
import br.com.apsoo.model.RelatorioDevolucoes;
import br.com.apsoo.model.RelatorioExportavel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

// Relatório de devoluções entre duas datas (RF22): somente administrador
public class RelatorioDevolucoesView extends VBox {

    private final DatePicker dpInicial = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dpFinal = new DatePicker(LocalDate.now());
    private final Indicador indDevolucoes = new Indicador("Devoluções (transações)").comIcone("devolucao", "");
    private final Indicador indItens = new Indicador("Itens devolvidos").comIcone("produtos", "");
    private final Indicador indValor = new Indicador("Valor reembolsado").comIcone("caixa", "warning");
    private final Indicador indMotivo = new Indicador("Motivo predominante").comIcone("recibo", "");
    private final ObservableList<RelatorioDevolucoes.CategoriaDevolvida> ranking = FXCollections.observableArrayList();
    private final ObservableList<Map.Entry<CondicaoProduto, Integer>> condicoes = FXCollections.observableArrayList();
    private final RelatorioController controller = new RelatorioController();
    private RelatorioExportavel exportacao; // resultado da última consulta exibida
    private final BotaoExportar btnExportar = new BotaoExportar(() -> exportacao);

    public RelatorioDevolucoesView() {
        setSpacing(16);

        Button btnGerar = Botoes.comIcone("Gerar", "atualizar", "accent");
        btnGerar.setOnAction(e -> atualizar());
        dpInicial.setPrefWidth(170);
        dpFinal.setPrefWidth(170);
        Fluxo filtro = new Fluxo(12, 8, new Campo("Data inicial", dpInicial), new Campo("Data final", dpFinal),
                new VBox(6, new Label(" "), btnGerar));

        GradeResponsiva indicadores = new GradeResponsiva(220, 16);
        indicadores.getChildren().addAll(indDevolucoes, indItens, indValor, indMotivo);

        TableView<RelatorioDevolucoes.CategoriaDevolvida> tabelaRanking = new TableView<>(ranking);
        tabelaRanking.getColumns().addAll(
                Celulas.<RelatorioDevolucoes.CategoriaDevolvida>posicao("#"),
                Celulas.<RelatorioDevolucoes.CategoriaDevolvida>chip("Categoria", 190,
                        RelatorioDevolucoes.CategoriaDevolvida::categoria),
                Celulas.<RelatorioDevolucoes.CategoriaDevolvida>medidor("Itens devolvidos", 170,
                        c -> c.quantidade(), c -> c.quantidade() + (c.quantidade() == 1 ? " item" : " itens"),
                        c -> "warning"));
        tabelaRanking.setPlaceholder(Tabelas.vazio("Nenhuma devolução no período"));
        Tabelas.preparar(tabelaRanking);
        tabelaRanking.setPrefHeight(300);

        TableView<Map.Entry<CondicaoProduto, Integer>> tabelaCondicoes = new TableView<>(condicoes);
        tabelaCondicoes.getColumns().addAll(
                Celulas.<Map.Entry<CondicaoProduto, Integer>>comIcone("Condição do produto", 230,
                        e -> e.getKey() == CondicaoProduto.INTEGRAL ? "confirmar" : "alerta",
                        e -> e.getKey() == CondicaoProduto.INTEGRAL ? "success" : "warning",
                        e -> e.getKey().toString(), null),
                Celulas.<Map.Entry<CondicaoProduto, Integer>>medidor("Itens devolvidos", 170,
                        e -> e.getValue(), e -> e.getValue() + (e.getValue() == 1 ? " item" : " itens"),
                        e -> e.getKey() == CondicaoProduto.INTEGRAL ? "success" : "warning"));
        Tabelas.preparar(tabelaCondicoes);
        tabelaCondicoes.setPrefHeight(300);

        GradeResponsiva tabelas = new GradeResponsiva(340, 16);
        tabelas.getChildren().addAll(Tabelas.comTitulo("Ranking de categorias por devoluções", tabelaRanking),
                Tabelas.comTitulo("Itens por condição", tabelaCondicoes));

        getChildren().addAll(new Barra().aEsquerda(filtro).aDireita(btnExportar), indicadores, tabelas);
        atualizar();
    }

    // As datas usadas na consulta são repassadas, e não as dos campos, que podem ter sido alteradas depois
    private RelatorioExportavel montarExportacao(RelatorioDevolucoes relatorio, LocalDate inicial, LocalDate fim) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RelatorioExportavel saida = new RelatorioExportavel("Relatório de devoluções");
        saida.adicionarFiltro("Data inicial", inicial.format(formato));
        saida.adicionarFiltro("Data final", fim.format(formato));
        saida.adicionarResumo("Devoluções (transações)", String.valueOf(relatorio.getQuantidadeDevolucoes()));
        saida.adicionarResumo("Itens devolvidos", String.valueOf(relatorio.getQuantidadeItens()));
        saida.adicionarResumo("Valor reembolsado", Tema.moeda(relatorio.getValorReembolsado()));
        saida.adicionarResumo("Motivo predominante",
                relatorio.getMotivoPredominante() == null ? "-" : relatorio.getMotivoPredominante());

        List<List<String>> rankingLinhas = relatorio.getRankingCategorias().stream()
                .map(c -> List.of(c.categoria(), String.valueOf(c.quantidade())))
                .toList();
        saida.adicionarSecao("Ranking de categorias por devoluções", List.of("Categoria", "Itens devolvidos"),
                rankingLinhas);

        List<List<String>> porCondicao = relatorio.getItensPorCondicao().entrySet().stream()
                .map(e -> List.of(e.getKey().toString(), String.valueOf(e.getValue())))
                .toList();
        saida.adicionarSecao("Itens por condição", List.of("Condição do produto", "Itens devolvidos"), porCondicao);
        return saida;
    }

    public final void atualizar() {
        try {
            RelatorioDevolucoes relatorio = controller.consultarDevolucoes(dpInicial.getValue(), dpFinal.getValue());
            indDevolucoes.setValor(String.valueOf(relatorio.getQuantidadeDevolucoes()));
            indItens.setValor(String.valueOf(relatorio.getQuantidadeItens()));
            indValor.setValor(Tema.moeda(relatorio.getValorReembolsado()));
            indMotivo.setValor(relatorio.getMotivoPredominante() == null ? "-" : relatorio.getMotivoPredominante());
            ranking.setAll(relatorio.getRankingCategorias());
            condicoes.setAll(relatorio.getItensPorCondicao().entrySet());
            exportacao = montarExportacao(relatorio, dpInicial.getValue(), dpFinal.getValue());
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
