package br.com.apsoo.view;

import br.com.apsoo.controller.ConfiguracaoController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.ModoAcao;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Preferências do usuário: onde abrem os formulários e as mensagens das ações
public class ConfiguracoesView extends VBox {

    private final ConfiguracaoController controller = new ConfiguracaoController();
    private final ToggleGroup grupo = new ToggleGroup();
    private final Label lblSalvo = new Label("Preferência salva.");

    public ConfiguracoesView() {
        setSpacing(16);

        Label titulo = new Label("Janelas de ação");
        titulo.getStyleClass().add("subtitle");
        Label descricao = new Label("Escolha onde abrem os formulários e as mensagens das ações, como cadastrar, "
                + "editar, pagar e confirmar.");
        descricao.getStyleClass().add("muted");
        descricao.setWrapText(true);

        GradeResponsiva opcoes = new GradeResponsiva(300, 16);
        ModoAcao atual = controller.getModoAcao();
        for (ModoAcao modo : ModoAcao.values()) {
            ToggleButton opcao = criarOpcao(modo);
            opcao.setSelected(modo == atual);
            opcoes.getChildren().add(opcao);
        }

        // Só grava quando o usuário muda a escolha (e não ao montar a tela)
        grupo.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo != null && novo.getUserData() instanceof ModoAcao modo) {
                salvar(modo);
            }
        });

        lblSalvo.getStyleClass().addAll("badge", "success");
        lblSalvo.setVisible(false);
        lblSalvo.setManaged(false);
        Label dica = new Label("A preferência vale para o seu usuário, neste computador.");
        dica.getStyleClass().addAll("muted", "hint");
        HBox rodape = new HBox(12, dica, lblSalvo);
        rodape.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(16, new VBox(4, titulo, descricao), opcoes, rodape);
        card.getStyleClass().add("card");
        card.setMaxWidth(820);
        getChildren().add(card);
    }

    private ToggleButton criarOpcao(ModoAcao modo) {
        StackPane icone = new StackPane(Icones.de(modo == ModoAcao.TELA ? "tela" : "janela", 22));
        icone.getStyleClass().add("icon-chip");

        Label lblTitulo = new Label(modo.toString());
        lblTitulo.getStyleClass().add("field-label");
        Label lblDescricao = new Label(modo.getDescricao());
        lblDescricao.getStyleClass().addAll("muted", "hint");
        lblDescricao.setWrapText(true);
        lblDescricao.setMaxWidth(260);
        VBox textos = new VBox(3, lblTitulo, lblDescricao);

        Region folga = new Region();
        HBox.setHgrow(folga, Priority.ALWAYS);
        HBox conteudo = new HBox(14, icone, textos, folga);
        if (modo == ModoAcao.TELA) {
            Label padrao = new Label("Padrão");
            padrao.setMinWidth(Region.USE_PREF_SIZE); // o selo nunca é cortado
            padrao.getStyleClass().addAll("badge", "neutral");
            conteudo.getChildren().add(padrao);
        }
        conteudo.setAlignment(Pos.CENTER_LEFT);

        ToggleButton opcao = new ToggleButton();
        opcao.setGraphic(conteudo);
        opcao.getStyleClass().add("opcao");
        opcao.setUserData(modo);
        opcao.setToggleGroup(grupo);
        opcao.setMaxWidth(Double.MAX_VALUE);
        // Uma opção sempre fica marcada
        opcao.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (opcao.isSelected()) {
                e.consume();
            }
        });
        return opcao;
    }

    private void salvar(ModoAcao modo) {
        try {
            controller.definirModoAcao(modo);
            lblSalvo.setVisible(true);
            lblSalvo.setManaged(true);
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        }
    }
}
