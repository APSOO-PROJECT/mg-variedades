package br.com.apsoo.view;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

// Página Caixa: situação atual e histórico
public class CaixaView extends TabPane {

    public CaixaView(Runnable aoMudar) {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        CaixaAtualView atual = new CaixaAtualView(aoMudar);
        HistoricoCaixaView historico = new HistoricoCaixaView();
        Tab abaAtual = new Tab("Caixa atual", atual);
        Tab abaHistorico = new Tab("Histórico", historico);
        getTabs().addAll(abaAtual, abaHistorico);

        abaAtual.setOnSelectionChanged(e -> {
            if (abaAtual.isSelected()) {
                atual.atualizar();
            }
        });
        abaHistorico.setOnSelectionChanged(e -> {
            if (abaHistorico.isSelected()) {
                historico.atualizar();
            }
        });
    }
}
