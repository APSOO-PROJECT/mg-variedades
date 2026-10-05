package br.com.apsoo.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import br.com.apsoo.controller.CaixaController;
import br.com.apsoo.controller.Sessao;
import br.com.apsoo.model.Usuario;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// Estrutura principal responsiva: menu lateral largo, menu só com ícones ou menu em gaveta,
// conforme a largura da janela; cabeçalho com status do caixa; conteúdo com rolagem
public class PrincipalView extends StackPane {

    private enum Modo { AMPLO, COMPACTO, GAVETA }

    private static final double LIMITE_AMPLO = 1000;
    private static final double LIMITE_COMPACTO = 720;
    private static final double LARGURA_AMPLO = 252;
    private static final double LARGURA_COMPACTO = 76;
    private static final double LARGURA_GAVETA = 280;

    private final CaixaController caixaController = new CaixaController();
    private final ToggleGroup grupoMenu = new ToggleGroup();
    private final List<ToggleButton> itens = new ArrayList<>();
    private final BorderPane estrutura = new BorderPane();
    private final VBox sidebar = new VBox(4);
    private final VBox menu = new VBox(4);
    private final Region scrim = new Region();
    private final Node logoCompleto = Logo.completo(128, Logo.AZUL);
    private final Node logoIcone = Logo.icone(34, Logo.AZUL);
    private HBox marcaBarra;
    private final VBox dadosUsuario = new VBox(2);
    private final Button btnSair = Botoes.comIcone("Sair", "sair");
    private final Button btnMenu = new Button();
    private final Label lblPagina = new Label();
    private final Label lblTextoCaixa = new Label("Caixa");
    private final Label lblCaixa = new Label();
    private final HBox cabecalho = new HBox(12);
    private final BorderPane conteudo = new BorderPane();
    private final ScrollPane rolagem = new ScrollPane(conteudo);

    private Modo modo;
    private boolean gavetaAberta;

    public PrincipalView(Runnable aoSair) {
        Usuario usuario = Sessao.getUsuarioLogado();

        montarMenuLateral(usuario, aoSair);
        montarCabecalho();

        rolagem.setFitToWidth(true);
        rolagem.setFitToHeight(true);
        conteudo.setFocusTraversable(true);

        // Menu em toda a altura; o cabeçalho fica só sobre a área de conteúdo
        BorderPane areaPrincipal = new BorderPane();
        areaPrincipal.setTop(cabecalho);
        areaPrincipal.setCenter(rolagem);
        estrutura.setCenter(areaPrincipal);

        scrim.getStyleClass().add("scrim");
        scrim.setOnMouseClicked(e -> fecharGaveta());

        getChildren().add(estrutura);
        Janela.registrarAnfitriao(this); // as janelas de ação aparecem sobre esta tela quando for a preferência
        widthProperty().addListener((obs, antigo, largura) -> definirModo(modoPara(largura.doubleValue())));
        definirModo(Modo.AMPLO);

        // Telas disponíveis
        adicionarItem("Dashboard", "dashboard", DashboardView::new);
        adicionarItem("Vendas", "vendas", VendasView::new);
        adicionarItem("Estoque", "estoque", EstoqueView::new);
        if (usuario.isAdmin()) {
            adicionarItem("Produtos", "produtos", ProdutosView::new);
            adicionarItem("Categorias", "categorias", CategoriasView::new);
            adicionarItem("Caixa", "caixa", () -> new CaixaView(this::atualizarStatusCaixa));
            adicionarItem("Relatórios", "relatorios", RelatoriosView::new);
            adicionarItem("Usuários", "usuarios", UsuariosView::new);
        }
        adicionarItem("Minha conta", "conta", MinhaContaView::new);
        adicionarItem("Configurações", "configuracoes", ConfiguracoesView::new);
        grupoMenu.getToggles().get(0).setSelected(true);

        // O status do caixa fica visível a qualquer momento (RF13)
        atualizarStatusCaixa();
        Timeline atualizador = new Timeline(new KeyFrame(Duration.seconds(30), e -> atualizarStatusCaixa()));
        atualizador.setCycleCount(Timeline.INDEFINITE);
        atualizador.play();
    }

    private void montarMenuLateral(Usuario usuario, Runnable aoSair) {
        // Fundo claro: logo em azul; no menu só de ícones, apenas o carrinho
        HBox marca = new HBox(logoCompleto, logoIcone);
        marca.setAlignment(Pos.CENTER_LEFT);
        marca.setPadding(new Insets(0, 0, 0, 10));
        marcaBarra = marca;
        marca.setFillHeight(false); // a logo mantém o tamanho próprio
        marca.setMinHeight(64); // mesma altura do cabeçalho, para as linhas se alinharem

        StackPane avatar = new StackPane(new Label(iniciais(usuario.getNome())));
        avatar.getStyleClass().add("avatar");
        Label lblNome = new Label(usuario.getNome());
        lblNome.setMaxWidth(150);
        lblNome.getStyleClass().add("field-label");
        Label lblCargo = new Label(usuario.isAdmin() ? "Administrador" : "Vendedor");
        lblCargo.getStyleClass().addAll("muted", "hint");
        dadosUsuario.getChildren().addAll(lblNome, lblCargo);
        HBox chipUsuario = new HBox(10, avatar, dadosUsuario);
        chipUsuario.setAlignment(Pos.CENTER_LEFT);
        chipUsuario.getStyleClass().add("user-chip");
        Tooltip.install(chipUsuario, new Tooltip(usuario.getNome() + " - " + lblCargo.getText()));

        btnSair.setMaxWidth(Double.MAX_VALUE);
        btnSair.setTooltip(new Tooltip("Sair do sistema"));
        btnSair.setOnAction(e -> aoSair.run());

        // O menu rola quando a janela é baixa; o usuário e o botão Sair ficam sempre visíveis
        ScrollPane rolagemMenu = new ScrollPane(menu);
        rolagemMenu.setFitToWidth(true);
        rolagemMenu.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(rolagemMenu, Priority.ALWAYS);
        VBox.setMargin(marca, new Insets(0, 0, 10, 0));
        VBox.setMargin(btnSair, new Insets(8, 0, 0, 0));

        sidebar.getChildren().addAll(marca, rolagemMenu, chipUsuario, btnSair);
        sidebar.getStyleClass().add("sidebar");
    }

    private void montarCabecalho() {
        btnMenu.setGraphic(Icones.de("menu", 20));
        btnMenu.getStyleClass().addAll("icone-btn", "ghost");
        btnMenu.setTooltip(new Tooltip("Abrir menu"));
        btnMenu.setOnAction(e -> abrirGaveta());

        lblPagina.getStyleClass().add("page-title");
        lblTextoCaixa.getStyleClass().add("muted");
        lblCaixa.getStyleClass().add("badge");

        Region folga = new Region();
        HBox.setHgrow(folga, Priority.ALWAYS);
        cabecalho.getChildren().addAll(btnMenu, lblPagina, folga, lblTextoCaixa, lblCaixa);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setMinHeight(64);
        cabecalho.getStyleClass().add("header");
    }

    private Modo modoPara(double largura) {
        if (largura >= LIMITE_AMPLO) {
            return Modo.AMPLO;
        }
        return largura >= LIMITE_COMPACTO ? Modo.COMPACTO : Modo.GAVETA;
    }

    // Reorganiza a interface quando a janela cruza um dos limites de largura
    private void definirModo(Modo novo) {
        if (novo == modo) {
            return;
        }
        modo = novo;
        boolean gaveta = modo == Modo.GAVETA;
        boolean compacto = modo == Modo.COMPACTO;

        // A barra lateral fica fixa na estrutura, exceto na gaveta, onde flutua sobre o conteúdo
        getChildren().removeAll(scrim, sidebar);
        gavetaAberta = false;
        estrutura.setLeft(gaveta ? null : sidebar);
        sidebar.getStyleClass().remove("drawer");
        if (gaveta) {
            sidebar.getStyleClass().add("drawer");
        }
        sidebar.setTranslateX(0);

        double largura = gaveta ? LARGURA_GAVETA : compacto ? LARGURA_COMPACTO : LARGURA_AMPLO;
        sidebar.setMinWidth(largura);
        sidebar.setPrefWidth(largura);
        sidebar.setMaxWidth(largura);
        sidebar.setMaxHeight(Double.MAX_VALUE);

        logoCompleto.setVisible(!compacto);
        logoCompleto.setManaged(!compacto);
        logoIcone.setVisible(compacto);
        logoIcone.setManaged(compacto);
        marcaBarra.setAlignment(compacto ? Pos.CENTER : Pos.CENTER_LEFT);
        dadosUsuario.setVisible(!compacto);
        dadosUsuario.setManaged(!compacto);
        btnSair.setContentDisplay(compacto ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT);
        for (ToggleButton item : itens) {
            aplicarModoItem(item);
        }

        btnMenu.setVisible(gaveta);
        btnMenu.setManaged(gaveta);
        lblTextoCaixa.setVisible(!gaveta);
        lblTextoCaixa.setManaged(!gaveta);
        double margem = gaveta ? 14 : compacto ? 20 : 24;
        conteudo.setPadding(new Insets(margem));
        cabecalho.setPadding(new Insets(14, margem, 14, margem));
    }

    private void aplicarModoItem(ToggleButton item) {
        boolean compacto = modo == Modo.COMPACTO;
        item.setContentDisplay(compacto ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT);
        item.getStyleClass().remove("compacto");
        if (compacto) {
            item.getStyleClass().add("compacto");
        }
    }

    private void abrirGaveta() {
        if (modo != Modo.GAVETA || gavetaAberta) {
            return;
        }
        gavetaAberta = true;
        StackPane.setAlignment(sidebar, Pos.TOP_LEFT);
        scrim.setOpacity(0);
        sidebar.setTranslateX(-LARGURA_GAVETA);
        getChildren().addAll(scrim, sidebar);

        FadeTransition escurecer = new FadeTransition(Duration.millis(180), scrim);
        escurecer.setToValue(1);
        TranslateTransition deslizar = new TranslateTransition(Duration.millis(180), sidebar);
        deslizar.setToX(0);
        escurecer.play();
        deslizar.play();
    }

    private void fecharGaveta() {
        if (!gavetaAberta) {
            return;
        }
        gavetaAberta = false;
        FadeTransition clarear = new FadeTransition(Duration.millis(150), scrim);
        clarear.setToValue(0);
        TranslateTransition recolher = new TranslateTransition(Duration.millis(150), sidebar);
        recolher.setToX(-LARGURA_GAVETA);
        recolher.setOnFinished(e -> {
            if (!gavetaAberta) {
                getChildren().removeAll(scrim, sidebar);
            }
        });
        clarear.play();
        recolher.play();
    }

    private void adicionarItem(String titulo, String icone, Supplier<Node> tela) {
        ToggleButton item = new ToggleButton(titulo);
        item.setGraphic(Icones.de(icone, 20));
        item.getStyleClass().addAll("button", "nav-item");
        item.setToggleGroup(grupoMenu);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setMnemonicParsing(false);
        item.setTooltip(new Tooltip(titulo));
        item.selectedProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado) {
                lblPagina.setText(titulo);
                conteudo.setCenter(tela.get());
                rolagem.setVvalue(0);
                fecharGaveta();
                // Tira o foco do primeiro botão da tela para ele não aparecer destacado ao abrir a página
                Platform.runLater(conteudo::requestFocus);
            }
        });
        // Impede que nenhum item fique desmarcado
        item.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (item.isSelected()) {
                e.consume();
                fecharGaveta();
            }
        });
        itens.add(item);
        menu.getChildren().add(item);
        aplicarModoItem(item);
    }

    private void atualizarStatusCaixa() {
        lblCaixa.getStyleClass().removeAll("success", "danger");
        try {
            boolean aberto = caixaController.isCaixaAberto();
            lblCaixa.setText(aberto ? "Aberto" : "Fechado");
            lblCaixa.getStyleClass().add(aberto ? "success" : "danger");
        } catch (SQLException ex) {
            lblCaixa.setText("Indisponível");
            lblCaixa.getStyleClass().add("danger");
        }
    }

    // "Maria da Silva" -> "MS"
    private String iniciais(String nome) {
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}
