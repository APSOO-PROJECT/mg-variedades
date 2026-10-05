package br.com.apsoo.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import br.com.apsoo.controller.ConfiguracaoController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.ModoAcao;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

// Janela de ação (formulários e mensagens). Conforme a preferência do usuário, aparece sobre a própria
// tela (padrão) ou em uma janela separada. Em ambos os casos o método exibir() bloqueia até o usuário
// confirmar ou cancelar, e devolve o resultado da confirmação (ou null se cancelou).
public class Janela<T> {

    private static final ConfiguracaoController CONFIGURACAO = new ConfiguracaoController();
    private static StackPane anfitriao;

    private final String titulo;
    private Node conteudo = new Region();
    private double largura = 460;
    private String textoConfirmar = "Confirmar";
    private String textoCancelar = "Cancelar";
    private Callable<T> aoConfirmar = () -> null;
    private final Label lblErro = new Label();

    private T resultado;
    private boolean encerrada;
    private Runnable fechador = () -> { };

    public Janela(String titulo) {
        this.titulo = titulo;
    }

    // A tela principal registra a si mesma para receber as janelas no modo "na própria tela"
    public static void registrarAnfitriao(StackPane raiz) {
        anfitriao = raiz;
    }

    public Janela<T> conteudo(Node conteudo) {
        this.conteudo = conteudo;
        return this;
    }

    public Janela<T> largura(double largura) {
        this.largura = largura;
        return this;
    }

    // A ação roda ao confirmar; se lançar uma exceção de regra de negócio, a mensagem aparece
    // na própria janela e ela continua aberta
    public Janela<T> confirmar(String texto, Callable<T> acao) {
        this.textoConfirmar = texto;
        this.aoConfirmar = acao;
        return this;
    }

    public Janela<T> cancelar(String texto) {
        this.textoCancelar = texto;
        return this;
    }

    public Janela<T> semCancelar() {
        this.textoCancelar = null;
        return this;
    }

    public T exibir() {
        boolean naTela = anfitriao != null && anfitriao.getScene() != null
                && CONFIGURACAO.getModoAcao() == ModoAcao.TELA;
        return naTela ? exibirNaTela() : exibirEmJanela();
    }

    // ------------------------------------------------------------------ modo "na própria tela"

    private T exibirNaTela() {
        StackPane host = anfitriao;

        Region cortina = new Region();
        cortina.getStyleClass().add("scrim");
        VBox cartao = montarCartao(true);
        Camada camada = new Camada(cortina, cartao, largura);

        // Bloqueia a tela de trás (inclusive o teclado) enquanto a janela estiver aberta
        List<Node> bloqueados = new ArrayList<>();
        List<Boolean> estadoAnterior = new ArrayList<>();
        for (Node filho : host.getChildren()) {
            bloqueados.add(filho);
            estadoAnterior.add(filho.isDisable());
            filho.setDisable(true);
        }

        camada.setOpacity(0);
        host.getChildren().add(camada);
        FadeTransition aparecer = new FadeTransition(Duration.millis(140), camada);
        aparecer.setToValue(1);
        aparecer.play();
        Platform.runLater(() -> focarPrimeiroCampo(cartao));

        Object chave = new Object();
        fechador = () -> Platform.exitNestedEventLoop(chave, null);
        Platform.enterNestedEventLoop(chave);

        host.getChildren().remove(camada);
        for (int i = 0; i < bloqueados.size(); i++) {
            bloqueados.get(i).setDisable(estadoAnterior.get(i));
        }
        return resultado;
    }

    // ------------------------------------------------------------------ modo "janela separada"

    private T exibirEmJanela() {
        Stage palco = new Stage();
        Window dono = anfitriao != null && anfitriao.getScene() != null ? anfitriao.getScene().getWindow() : null;
        if (dono != null) {
            palco.initOwner(dono);
        }
        palco.initModality(Modality.APPLICATION_MODAL);
        palco.setTitle(titulo);

        VBox cartao = montarCartao(false);
        Scene cena = new Scene(cartao);
        Tema.aplicar(cena);

        // Os controles só informam o tamanho correto depois de aparecerem (é quando o CSS cria suas "peles").
        // Por isso a janela abre com uma altura provisória e, ao aparecer, ajusta-se à altura do conteúdo,
        // limitada à da tela, e se recentraliza.
        cartao.setPrefSize(largura, 320);
        palco.setScene(cena);
        boolean[] primeiraVez = {true};
        Runnable ajustar = () -> {
            cartao.setPrefHeight(Region.USE_COMPUTED_SIZE); // senão prefHeight devolveria a altura provisória
            cartao.applyCss();
            cartao.layout();
            double alturaMaxima = Screen.getPrimary().getVisualBounds().getHeight() * 0.85;
            double altura = Math.min(cartao.prefHeight(largura), alturaMaxima);
            double molduras = palco.getHeight() - cena.getHeight();
            palco.setHeight(altura + molduras);
            if (primeiraVez[0]) {
                palco.centerOnScreen();
                primeiraVez[0] = false;
            }
        };
        palco.setOnShown(e -> Platform.runLater(ajustar));
        // A mensagem de erro ocupa espaço novo: a janela cresce para os campos não ficarem escondidos
        lblErro.textProperty().addListener((obs, antigo, novo) -> Platform.runLater(ajustar));
        palco.setMinWidth(Math.min(largura, 360));
        fechador = palco::close;
        Platform.runLater(() -> focarPrimeiroCampo(cartao));
        palco.showAndWait();
        return resultado;
    }

    // ------------------------------------------------------------------ conteúdo comum

    private VBox montarCartao(boolean naTela) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("subtitle");
        Region folga = new Region();
        HBox.setHgrow(folga, Priority.ALWAYS);
        HBox cabecalho = new HBox(8, lblTitulo, folga);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        if (textoCancelar != null) {
            cabecalho.getChildren().add(Botoes.acao("fechar", "Fechar", () -> encerrar(null)));
        }

        ScrollPane rolagem = new RolagemAjustada(conteudo);
        rolagem.setFitToWidth(true);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(rolagem, Priority.ALWAYS);

        lblErro.getStyleClass().add("erro");
        lblErro.setWrapText(true);
        lblErro.managedProperty().bind(lblErro.textProperty().isNotEmpty());
        lblErro.visibleProperty().bind(lblErro.textProperty().isNotEmpty());

        Button btnConfirmar = new Button(textoConfirmar);
        btnConfirmar.getStyleClass().add("accent");
        btnConfirmar.setMnemonicParsing(false);
        btnConfirmar.setOnAction(e -> confirmar());
        HBox acoes = new HBox(10);
        acoes.setAlignment(Pos.CENTER_RIGHT);
        if (textoCancelar != null) {
            Button btnCancelar = new Button(textoCancelar);
            btnCancelar.setMnemonicParsing(false);
            btnCancelar.setOnAction(e -> encerrar(null));
            acoes.getChildren().add(btnCancelar);
        }
        acoes.getChildren().add(btnConfirmar);

        VBox cartao = new VBox(16);
        if (naTela) {
            cartao.getChildren().add(cabecalho);
        }
        cartao.getChildren().addAll(rolagem, lblErro, acoes);
        cartao.getStyleClass().add(naTela ? "janela-cartao" : "janela-raiz");

        // Enter confirma e Esc cancela, sem interferir em botões e listas abertas
        cartao.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            boolean emBotao = e.getTarget() instanceof Button;
            boolean emLista = e.getTarget() instanceof ComboBoxBase<?> combo && combo.isShowing();
            if (e.getCode() == KeyCode.ENTER && !emBotao && !(e.getTarget() instanceof ComboBoxBase)) {
                e.consume();
                confirmar();
            } else if (e.getCode() == KeyCode.ESCAPE && textoCancelar != null && !emLista) {
                e.consume();
                encerrar(null);
            }
        });
        return cartao;
    }

    private void confirmar() {
        if (encerrada) {
            return;
        }
        lblErro.setText("");
        try {
            encerrar(aoConfirmar.call());
        } catch (RegraNegocioException | IllegalArgumentException ex) {
            lblErro.setText(ex.getMessage());
        } catch (SQLException ex) {
            lblErro.setText("Erro na operação: " + ex.getMessage());
        } catch (Exception ex) {
            lblErro.setText("Erro inesperado: " + ex.getMessage());
        }
    }

    private void encerrar(T valor) {
        if (encerrada) {
            return;
        }
        encerrada = true;
        resultado = valor;
        fechador.run();
    }

    private void focarPrimeiroCampo(Node cartao) {
        Node campo = cartao.lookup(".text-field");
        if (campo != null && !campo.isDisabled()) {
            campo.requestFocus();
        }
    }

    // ScrollPane que informa como altura preferida a do próprio conteúdo para a largura dada (o padrão do
    // JavaFX só calcula isso depois de pronto, e a janela abriria baixa demais)
    private static final class RolagemAjustada extends ScrollPane {
        RolagemAjustada(Node conteudo) {
            super(conteudo);
        }

        @Override
        protected double computePrefHeight(double largura) {
            Node conteudo = getContent();
            double larguraUtil = largura > 0 ? largura - snappedLeftInset() - snappedRightInset() : -1;
            return (conteudo == null ? 0 : conteudo.prefHeight(larguraUtil)) + snappedTopInset() + snappedBottomInset();
        }
    }

    // Camada sobre a tela: cortina escura em toda a área e o cartão centralizado, com a largura
    // limitada (a janela estreita encolhe o cartão) e a altura limitada ao espaço disponível
    private static final class Camada extends Pane {
        private static final double MARGEM = 24;

        private final Region cortina;
        private final Node cartao;
        private final double larguraMaxima;

        Camada(Region cortina, Node cartao, double larguraMaxima) {
            this.cortina = cortina;
            this.cartao = cartao;
            this.larguraMaxima = larguraMaxima;
            getChildren().addAll(cortina, cartao);
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            cortina.resizeRelocate(0, 0, w, h);

            double larguraCartao = Math.min(larguraMaxima, Math.max(0, w - 2 * MARGEM));
            double alturaCartao = Math.min(cartao.prefHeight(larguraCartao), Math.max(0, h - 2 * MARGEM));
            cartao.resizeRelocate((w - larguraCartao) / 2, (h - alturaCartao) / 2, larguraCartao, alturaCartao);
        }
    }
}
