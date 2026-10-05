package br.com.apsoo.view;

import java.sql.SQLException;

import br.com.apsoo.controller.DashboardController;
import br.com.apsoo.controller.Sessao;
import br.com.apsoo.model.MetricasDia;
import br.com.apsoo.model.Usuario;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

// Painel inicial (RF17): métricas desde a abertura do caixa vigente, variando conforme o perfil
public class DashboardView extends VBox {

    private final DashboardController controller = new DashboardController();
    private final Usuario usuario = Sessao.getUsuarioLogado();
    private final GradeResponsiva indicadores = new GradeResponsiva(220, 16);
    private final Label lblAviso = new Label();

    public DashboardView() {
        setSpacing(20);

        Label saudacao = new Label("Olá, " + usuario.getNome());
        saudacao.getStyleClass().add("subtitle");
        lblAviso.getStyleClass().add("muted");
        lblAviso.setWrapText(true);
        VBox textos = new VBox(4, saudacao, lblAviso);

        Button btnAtualizar = Botoes.comIcone("Atualizar", "atualizar");
        btnAtualizar.setOnAction(e -> atualizar());

        getChildren().addAll(new Barra().aEsquerda(textos).aDireita(btnAtualizar), indicadores);
        atualizar();
    }

    public final void atualizar() {
        indicadores.getChildren().clear();
        try {
            MetricasDia metricas = controller.carregar();

            lblAviso.setText(metricas.isCaixaAberto()
                    ? "Números desde a abertura do caixa vigente."
                    : "O caixa está fechado. As métricas do dia começam quando o caixa for aberto.");

            // Faturamento total da loja é informação do administrador
            if (usuario.isAdmin()) {
                indicadores.getChildren().addAll(
                        new Indicador("Faturamento do dia", Tema.moeda(metricas.getFaturamento()))
                                .comIcone("caixa", "success"),
                        new Indicador("Vendas realizadas", String.valueOf(metricas.getQuantidadeVendas()))
                                .comIcone("vendas", ""));
            } else {
                indicadores.getChildren().add(
                        new Indicador("Minhas vendas", String.valueOf(metricas.getQuantidadeVendas()))
                                .comIcone("vendas", ""));
            }
            indicadores.getChildren().addAll(
                    new Indicador("Produto mais vendido do dia", metricas.getProdutoMaisVendido() == null
                            ? "-" : metricas.getProdutoMaisVendido().toString()).comIcone("estrela", "warning"),
                    new Indicador("Produtos com estoque baixo", String.valueOf(metricas.getProdutosEstoqueBaixo()))
                            .comIcone("alerta", metricas.getProdutosEstoqueBaixo() > 0 ? "danger" : "success"));
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }
}
