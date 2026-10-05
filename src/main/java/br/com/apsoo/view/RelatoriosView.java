package br.com.apsoo.view;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

// Página Relatórios (somente administrador)
public class RelatoriosView extends TabPane {

    public RelatoriosView() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(
                new Tab("Vendas por período", new RelatorioVendasView()),
                new Tab("Mais vendidos", new MaisVendidosView()),
                new Tab("Por vendedor", new VendasPorVendedorView()),
                new Tab("Produtos em falta", new ProdutosEmFaltaView()),
                new Tab("Devoluções", new RelatorioDevolucoesView()));
    }
}
