package br.com.apsoo.view;

import java.sql.SQLException;
import java.util.List;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.RelatorioController;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.RelatorioExportavel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Relatório de produtos em falta (RF18): somente administrador
public class ProdutosEmFaltaView extends VBox {

    private final FiltroPeriodo filtro = new FiltroPeriodo(this::atualizar);
    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private final RelatorioController controller = new RelatorioController();
    private RelatorioExportavel exportacao; // resultado da última consulta exibida
    private final BotaoExportar btnExportar = new BotaoExportar(() -> exportacao);

    public ProdutosEmFaltaView() {
        setSpacing(16);

        Label dica = new Label("Produtos que atingiram a quantidade mínima e tiveram o estoque atualizado "
                + "(venda ou ajuste) no período selecionado.");
        dica.getStyleClass().add("muted");
        dica.setWrapText(true);

        TableView<Produto> tabela = new TableView<>(produtos);
        tabela.getColumns().addAll(
                Celulas.<Produto>comIcone("Produto", 220, p -> "alerta", p -> "danger", Produto::getNome, null),
                Celulas.<Produto>chip("Categoria", 170, p -> p.getCategoria().getNome()),
                Celulas.<Produto>medidor("Qtd. atual", 160, p -> p.getQuantidadeAtual(),
                        p -> p.getQuantidadeAtual() + " un.", p -> "danger"),
                Celulas.<Produto>numerica("Qtd. mínima", 120, p -> String.valueOf(p.getQuantidadeMinima())),
                Tabelas.<Produto>coluna("Última atualização", 160, p -> Tema.dataHora(p.getDataAtualizacao())),
                Celulas.<Produto>pessoa("Atualizado por", 190,
                        p -> p.getUsuarioAtualizacaoNome() == null ? "-" : p.getUsuarioAtualizacaoNome(), null));
        tabela.setPlaceholder(Tabelas.vazio("Nenhum produto em falta no período"));
        Tabelas.preparar(tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        getChildren().addAll(btnExportar.barraCom(filtro), dica, tabela);
        atualizar();
    }

    private RelatorioExportavel montarExportacao() {
        RelatorioExportavel saida = new RelatorioExportavel("Relatório de produtos em falta");
        saida.adicionarFiltro("Período", filtro.getPeriodo().descrever(filtro.getReferencia()));
        saida.adicionarFiltro("Critério", "Quantidade mínima atingida e estoque atualizado no período");

        List<List<String>> linhas = produtos.stream()
                .map(p -> List.of(p.getNome(), p.getCategoria().getNome(), String.valueOf(p.getQuantidadeAtual()),
                        String.valueOf(p.getQuantidadeMinima()), Tema.dataHora(p.getDataAtualizacao()),
                        p.getUsuarioAtualizacaoNome() == null ? "-" : p.getUsuarioAtualizacaoNome()))
                .toList();
        saida.adicionarSecao("Produtos em falta",
                List.of("Produto", "Categoria", "Qtd. atual", "Qtd. mínima", "Última atualização", "Atualizado por"),
                linhas);
        return saida;
    }

    public final void atualizar() {
        try {
            produtos.setAll(controller.consultarProdutosEmFalta(filtro.getPeriodo(), filtro.getReferencia()));
            exportacao = montarExportacao();
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
