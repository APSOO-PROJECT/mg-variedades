package br.com.apsoo.view;

import java.sql.SQLException;
import java.util.List;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.RelatorioController;
import br.com.apsoo.model.RelatorioExportavel;
import br.com.apsoo.model.VendasVendedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Relatório de vendas por vendedor (RF21): somente administrador
public class VendasPorVendedorView extends VBox {

    private final FiltroPeriodo filtro = new FiltroPeriodo(this::atualizar);
    private final ObservableList<VendasVendedor> vendedores = FXCollections.observableArrayList();
    private final RelatorioController controller = new RelatorioController();
    private RelatorioExportavel exportacao; // resultado da última consulta exibida
    private final BotaoExportar btnExportar = new BotaoExportar(() -> exportacao);

    public VendasPorVendedorView() {
        setSpacing(16);

        TableView<VendasVendedor> tabela = new TableView<>(vendedores);
        tabela.getColumns().addAll(
                Celulas.<VendasVendedor>posicao("#"),
                Celulas.<VendasVendedor>pessoa("Vendedor", 240, VendasVendedor::vendedor, null),
                Celulas.<VendasVendedor>numerica("Vendas realizadas", 160, v -> String.valueOf(v.quantidadeVendas())),
                Celulas.<VendasVendedor>medidor("Valor total vendido", 220, v -> v.valorTotal().doubleValue(),
                        v -> Tema.moeda(v.valorTotal()), v -> "success"));
        tabela.setPlaceholder(Tabelas.vazio("Nenhum usuário cadastrado"));
        Tabelas.preparar(tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        getChildren().addAll(btnExportar.barraCom(filtro), tabela);
        atualizar();
    }

    private RelatorioExportavel montarExportacao() {
        RelatorioExportavel saida = new RelatorioExportavel("Relatório de vendas por vendedor");
        saida.adicionarFiltro("Período", filtro.getPeriodo().descrever(filtro.getReferencia()));

        List<List<String>> linhas = vendedores.stream()
                .map(v -> List.of(v.vendedor(), String.valueOf(v.quantidadeVendas()), Tema.moeda(v.valorTotal())))
                .toList();
        saida.adicionarSecao("Vendas por vendedor", List.of("Vendedor", "Vendas realizadas", "Valor total vendido"),
                linhas);
        return saida;
    }

    public final void atualizar() {
        try {
            vendedores.setAll(controller.consultarVendasPorVendedor(filtro.getPeriodo(), filtro.getReferencia()));
            exportacao = montarExportacao();
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
