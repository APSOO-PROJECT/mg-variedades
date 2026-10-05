package br.com.apsoo.view;

import java.math.BigDecimal;
import java.sql.SQLException;

import br.com.apsoo.controller.CaixaController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.Caixa;
import br.com.apsoo.model.ResumoCaixa;
import br.com.apsoo.model.TipoOperacao;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Abrir caixa, acompanhar os totais, movimentar e fechar (somente administrador)
public class CaixaAtualView extends VBox {

    private final CaixaController controller = new CaixaController();
    private final Runnable aoMudar;

    public CaixaAtualView(Runnable aoMudar) {
        this.aoMudar = aoMudar;
        setSpacing(16);
        atualizar();
    }

    public void atualizar() {
        getChildren().clear();
        try {
            Caixa caixa = controller.buscarAberto();
            if (caixa == null) {
                getChildren().add(montarCaixaFechado());
            } else {
                montarCaixaAberto(caixa, controller.resumir(caixa));
            }
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private VBox montarCaixaFechado() {
        StackPane icone = new StackPane(Icones.de("cadeado", 22));
        icone.getStyleClass().addAll("icon-chip", "danger");
        Label titulo = new Label("Caixa fechado");
        titulo.getStyleClass().add("subtitle");
        Label dica = new Label("Informe o valor inicial (fundo de troco) para abrir o caixa. "
                + "Nenhuma venda pode ser registrada com o caixa fechado.");
        dica.getStyleClass().add("muted");
        dica.setWrapText(true);
        HBox cabecalho = new HBox(14, icone, new VBox(4, titulo, dica));
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        TextField txtFundo = new TextField();
        txtFundo.setPromptText("0,00");

        Button btnAbrir = Botoes.comIcone("Abrir caixa", "ativar", "success");

        Runnable abrir = () -> {
            try {
                controller.abrirCaixa(Tema.lerValor(txtFundo.getText()));
                aoMudar.run();
                atualizar();
            } catch (NumberFormatException ex) {
                Dialogos.avisar("Informe um valor inicial (fundo de troco) válido.");
            } catch (RegraNegocioException ex) {
                Dialogos.avisar(ex.getMessage());
            } catch (SQLException ex) {
                Dialogos.erro("Erro na operação: " + ex.getMessage());
            }
        };
        btnAbrir.setOnAction(e -> abrir.run());
        txtFundo.setOnAction(e -> abrir.run());

        HBox linhaFundo = new HBox(8, txtFundo, btnAbrir);
        HBox.setHgrow(txtFundo, Priority.ALWAYS);
        Campo campoFundo = new Campo("Fundo de troco (R$) *", linhaFundo);
        campoFundo.setMaxWidth(520);

        VBox card = new VBox(18, cabecalho, campoFundo);
        card.getStyleClass().add("card");
        return card;
    }

    private void montarCaixaAberto(Caixa caixa, ResumoCaixa resumo) {
        Label badge = new Label("Caixa aberto");
        badge.getStyleClass().addAll("badge", "success");
        Label desde = new Label("Aberto em " + Tema.dataHora(caixa.getDataHoraAbertura())
                + " por " + caixa.getUsuarioAberturaNome());
        desde.getStyleClass().add("muted");
        desde.setWrapText(true);
        HBox situacao = new HBox(12, badge, desde);
        situacao.setAlignment(Pos.CENTER_LEFT);

        Button btnSuprimento = Botoes.comIcone("Suprimento", "mais");
        btnSuprimento.setOnAction(e -> movimentar(TipoOperacao.SUPRIMENTO));
        Button btnSangria = Botoes.comIcone("Sangria", "menos");
        btnSangria.setOnAction(e -> movimentar(TipoOperacao.SANGRIA));
        Button btnFechar = Botoes.comIcone("Fechar caixa", "cadeado", "danger");
        btnFechar.setOnAction(e -> fechar(resumo));

        GradeResponsiva grade = new GradeResponsiva(220, 16);
        grade.getChildren().addAll(
                new Indicador("Fundo de troco", Tema.moeda(resumo.getFundoTrocoInicial())).comIcone("caixa", ""),
                new Indicador("Vendas em dinheiro", Tema.moeda(resumo.getVendasDinheiro())).comIcone("vendas", ""),
                new Indicador("Vendas no Pix", Tema.moeda(resumo.getVendasPix())).comIcone("vendas", ""),
                new Indicador("Vendas no cartão", Tema.moeda(resumo.getVendasCartao())).comIcone("cartao", ""),
                new Indicador("Suprimentos", Tema.moeda(resumo.getSuprimentos())).comIcone("mais", "success"),
                new Indicador("Sangrias", Tema.moeda(resumo.getSangrias())).comIcone("menos", "warning"),
                new Indicador("Reembolsos em dinheiro", Tema.moeda(resumo.getReembolsosDinheiro()))
                        .comIcone("devolucao", "warning"),
                new Indicador("Saldo esperado em dinheiro", Tema.moeda(resumo.calcularSaldoEsperado()))
                        .comIcone("confirmar", "success"));

        getChildren().addAll(new Barra().aEsquerda(situacao).aDireita(btnSuprimento, btnSangria, btnFechar), grade);
    }

    private void movimentar(TipoOperacao tipo) {
        if (MovimentacaoDialog.exibir(controller, tipo)) {
            atualizar();
        }
    }

    private void fechar(ResumoCaixa resumo) {
        Caixa fechado = FechamentoDialog.exibir(controller, resumo);
        if (fechado == null) {
            return;
        }

        StringBuilder mensagem = new StringBuilder("Caixa fechado com sucesso.")
                .append("\nValor esperado: ").append(Tema.moeda(fechado.getSaldoEsperado()))
                .append("\nValor contado: ").append(Tema.moeda(fechado.getSaldoReal()));
        if (fechado.calcularQuebra().signum() != 0) {
            mensagem.append("\nQuebra de caixa: ").append(Tema.moeda(fechado.calcularQuebra()));
        }
        mensagem.append("\nTotal vendido: ").append(Tema.moeda(resumo.calcularTotalVendas()))
                .append("\nLucro (considerando o custo de aquisição): ").append(Tema.moeda(resumo.getLucro()));
        Dialogos.informar(mensagem.toString());

        aoMudar.run();
        atualizar();
    }
}
