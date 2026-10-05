package br.com.apsoo.view;

import java.math.BigDecimal;
import java.sql.SQLException;

import br.com.apsoo.controller.CaixaController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.EventoCaixa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Histórico de aberturas, fechamentos, sangrias e suprimentos; somente leitura (RF13)
public class HistoricoCaixaView extends VBox {

    private final FiltroPeriodo filtro = new FiltroPeriodo(this::atualizar);
    private final TableView<EventoCaixa> tabela = new TableView<>();
    private final ObservableList<EventoCaixa> eventos = FXCollections.observableArrayList();
    private final CaixaController controller = new CaixaController();

    public HistoricoCaixaView() {
        setSpacing(16);
        getChildren().addAll(filtro, montarTabela());
        VBox.setVgrow(tabela, Priority.ALWAYS);
        atualizar();
    }

    private TableView<EventoCaixa> montarTabela() {
        tabela.getColumns().addAll(
                Tabelas.<EventoCaixa>coluna("Data/hora", 140, e -> Tema.dataHora(e.getDataHora())),
                Celulas.<EventoCaixa>comIcone("Evento", 190, this::icone, this::estilo, EventoCaixa::getEvento, null),
                Celulas.<EventoCaixa>destaque("Valor", 110, e -> Tema.moeda(e.getValor())),
                Tabelas.<EventoCaixa>coluna("Detalhe", 340, this::descreverDetalhe),
                Tabelas.<EventoCaixa>coluna("Usuário", 140, EventoCaixa::getUsuarioNome),
                Tabelas.<EventoCaixa>coluna("Caixa nº", 90, e -> String.valueOf(e.getCaixaId())));
        tabela.setItems(eventos);
        tabela.setPlaceholder(Tabelas.vazio("Nenhum registro no período"));
        Tabelas.preparar(tabela);
        return tabela;
    }

    private String icone(EventoCaixa evento) {
        return switch (evento.getEvento()) {
            case "Abertura" -> "ativar";
            case "Fechamento" -> "cadeado";
            case "Sangria" -> "menos";
            case "Suprimento" -> "mais";
            default -> "devolucao";
        };
    }

    // Verde: entra ou abre; vermelho: fecha; âmbar: saída de dinheiro
    private String estilo(EventoCaixa evento) {
        return switch (evento.getEvento()) {
            case "Abertura", "Suprimento" -> "success";
            case "Fechamento" -> "danger";
            default -> "warning";
        };
    }

    private String descreverDetalhe(EventoCaixa evento) {
        String detalhe = evento.getDetalhe() == null ? "" : evento.getDetalhe();
        if (evento.getValorEsperado() == null) {
            return detalhe;
        }
        // Fechamento: valor = contado; mostra o esperado e a quebra, se houver
        BigDecimal quebra = evento.getValor().subtract(evento.getValorEsperado());
        if (quebra.signum() == 0) {
            return "Esperado " + Tema.moeda(evento.getValorEsperado()) + " - sem divergência";
        }
        return "Esperado " + Tema.moeda(evento.getValorEsperado()) + " - quebra " + Tema.moeda(quebra)
                + (detalhe.isEmpty() ? "" : " - " + detalhe);
    }

    // Chamado também ao abrir a aba, para refletir movimentações recentes
    public void atualizar() {
        try {
            eventos.setAll(controller.consultarHistorico(filtro.getPeriodo(), filtro.getReferencia()));
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
