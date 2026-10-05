package br.com.apsoo.view;

import br.com.apsoo.controller.Sessao;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

// Página Vendas: registrar venda, localizar venda/devolução e, para o administrador, vendas por categoria
public class VendasView extends TabPane {

    public VendasView() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(
                new Tab("Nova venda", new VendaView()),
                new Tab("Localizar venda / Devolução", new DevolucaoView()));

        if (Sessao.getUsuarioLogado().isAdmin()) {
            getTabs().add(new Tab("Vendas por categoria", new VendasPorCategoriaView()));
        }
    }
}
