package br.com.apsoo.view;

import java.math.BigDecimal;
import java.util.function.Supplier;

import br.com.apsoo.model.Cartao;
import br.com.apsoo.model.Dinheiro;
import br.com.apsoo.model.FormaPagamento;
import br.com.apsoo.model.ModalidadeCartao;
import br.com.apsoo.model.Pagamento;
import br.com.apsoo.model.Pix;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Passo "Pagamento" da venda: o vendedor escolhe a forma (dinheiro, Pix ou cartão) e informa os dados da
// variante escolhida, que aparecem logo abaixo, na própria tela
public class PainelPagamento extends VBox {

    private final BigDecimal total;
    private final Runnable aoConfirmar;
    private final ToggleGroup grupo = new ToggleGroup();
    private final VBox detalhes = new VBox(14);
    private FormaPagamento forma;
    private Supplier<Pagamento> montador;

    // aoConfirmar roda quando o vendedor aperta Enter em um dos campos
    public PainelPagamento(BigDecimal total, Runnable aoConfirmar) {
        super(20);
        this.total = total;
        this.aoConfirmar = aoConfirmar;

        GradeResponsiva opcoes = new GradeResponsiva(200, 12);
        for (FormaPagamento candidata : FormaPagamento.values()) {
            opcoes.getChildren().add(criarOpcao(candidata));
        }
        getChildren().addAll(opcoes, detalhes);

        grupo.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            forma = novo == null ? null : (FormaPagamento) novo.getUserData();
            montarDetalhes();
        });
    }

    public FormaPagamento getForma() {
        return forma;
    }

    // Cria o pagamento com os dados informados; lança IllegalArgumentException com a mensagem a exibir
    public Pagamento criar() {
        if (montador == null) {
            throw new IllegalArgumentException("Método de pagamento não preenchido");
        }
        return montador.get();
    }

    private ToggleButton criarOpcao(FormaPagamento candidata) {
        String icone = switch (candidata) {
            case DINHEIRO -> "caixa";
            case PIX -> "recibo";
            case CARTAO -> "cartao";
        };
        String descricao = switch (candidata) {
            case DINHEIRO -> "Calcula o troco na hora";
            case PIX -> "Código do pagamento";
            case CARTAO -> "Débito ou crédito";
        };

        StackPane quadro = new StackPane(Icones.de(icone, 24));
        quadro.getStyleClass().add("icon-chip");
        Label lblTitulo = new Label(candidata.toString());
        lblTitulo.getStyleClass().add("subtitle");
        Label lblDescricao = new Label(descricao);
        lblDescricao.getStyleClass().addAll("muted", "hint");
        HBox conteudo = new HBox(14, quadro, new VBox(2, lblTitulo, lblDescricao));
        conteudo.setAlignment(Pos.CENTER_LEFT);

        ToggleButton opcao = new ToggleButton();
        opcao.setGraphic(conteudo);
        opcao.getStyleClass().add("opcao");
        opcao.setUserData(candidata);
        opcao.setToggleGroup(grupo);
        opcao.setMaxWidth(Double.MAX_VALUE);
        // Uma opção, uma vez escolhida, continua marcada
        opcao.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (opcao.isSelected()) {
                e.consume();
            }
        });
        return opcao;
    }

    private void montarDetalhes() {
        detalhes.getChildren().clear();
        montador = null;
        if (forma == null) {
            return;
        }
        switch (forma) {
            case DINHEIRO -> montarDinheiro();
            case PIX -> montarPix();
            case CARTAO -> montarCartao();
        }
    }

    private void montarDinheiro() {
        TextField txtRecebido = new TextField();
        txtRecebido.getStyleClass().add("grande");
        txtRecebido.setPromptText("0,00");
        txtRecebido.setOnAction(e -> aoConfirmar.run());

        Label lblTroco = new Label();
        lblTroco.getStyleClass().add("total");
        atualizarTroco(lblTroco, null);
        txtRecebido.textProperty().addListener((obs, antigo, novo) -> atualizarTroco(lblTroco, novo));

        // Atalhos de valores comuns, para agilizar o atendimento
        FlowPane atalhos = new FlowPane(8, 8);
        atalhos.getChildren().add(atalho("Valor exato", total, txtRecebido));
        for (int valor : new int[]{20, 50, 100}) {
            atalhos.getChildren().add(atalho("R$ " + valor, BigDecimal.valueOf(valor), txtRecebido));
        }

        detalhes.getChildren().addAll(new Campo("Valor recebido (R$) *", txtRecebido), atalhos, lblTroco);
        Platform.runLater(txtRecebido::requestFocus);

        montador = () -> {
            BigDecimal recebido;
            try {
                recebido = Tema.lerValor(txtRecebido.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Informe o valor recebido.");
            }
            Dinheiro dinheiro = new Dinheiro(total, recebido);
            if (!dinheiro.isValorSuficiente()) {
                throw new IllegalArgumentException("A quantia informada não é suficiente.");
            }
            return dinheiro;
        };
    }

    private Button atalho(String texto, BigDecimal valor, TextField campo) {
        Button botao = new Button(texto);
        botao.getStyleClass().add("compacto");
        botao.setFocusTraversable(false);
        botao.setOnAction(e -> {
            campo.setText(valor.toPlainString().replace('.', ','));
            campo.requestFocus();
            campo.positionCaret(campo.getText().length());
        });
        return botao;
    }

    // Mostra o troco, ou quanto ainda falta quando o valor recebido não cobre a venda
    private void atualizarTroco(Label rotulo, String texto) {
        rotulo.getStyleClass().removeAll("erro", "troco-ok");
        try {
            BigDecimal recebido = Tema.lerValor(texto == null ? "" : texto);
            Dinheiro previa = new Dinheiro(total, recebido);
            if (previa.isValorSuficiente()) {
                rotulo.setText("Troco: " + Tema.moeda(previa.calcularTroco()));
                rotulo.getStyleClass().add("troco-ok");
            } else {
                rotulo.setText("Faltam " + Tema.moeda(total.subtract(recebido)));
                rotulo.getStyleClass().add("erro");
            }
        } catch (NumberFormatException | NullPointerException e) {
            rotulo.setText("Troco: " + Tema.moeda(BigDecimal.ZERO));
        }
    }

    private void montarPix() {
        TextField txtCodigo = new TextField();
        txtCodigo.getStyleClass().add("grande");
        txtCodigo.setPromptText("Código do pagamento");
        txtCodigo.setOnAction(e -> aoConfirmar.run());

        Label dica = new Label("Insira o valor da venda na máquina e informe o código gerado pelo pagamento.");
        dica.getStyleClass().add("muted");
        dica.setWrapText(true);

        detalhes.getChildren().addAll(new Campo("Código do pagamento *", txtCodigo), dica);
        Platform.runLater(txtCodigo::requestFocus);

        montador = () -> {
            if (txtCodigo.getText().isBlank()) {
                throw new IllegalArgumentException("Informe o código do pagamento Pix.");
            }
            return new Pix(total, txtCodigo.getText().trim());
        };
    }

    private void montarCartao() {
        ComboBox<ModalidadeCartao> cbModalidade = new ComboBox<>();
        cbModalidade.getItems().addAll(ModalidadeCartao.values());
        cbModalidade.getSelectionModel().selectFirst();

        ComboBox<Integer> cbParcelas = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            cbParcelas.getItems().add(i);
        }
        cbParcelas.getSelectionModel().selectFirst();

        // Débito é sempre à vista
        cbParcelas.disableProperty().bind(cbModalidade.valueProperty().isEqualTo(ModalidadeCartao.DEBITO));
        cbModalidade.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo == ModalidadeCartao.DEBITO) {
                cbParcelas.getSelectionModel().selectFirst();
            }
        });

        Label dica = new Label("Insira o valor da venda na máquina e escolha débito ou crédito.");
        dica.getStyleClass().add("muted");
        dica.setWrapText(true);

        GradeResponsiva grade = new GradeResponsiva(200, 14);
        grade.getChildren().addAll(new Campo("Tipo *", cbModalidade), new Campo("Parcelas", cbParcelas));
        detalhes.getChildren().addAll(grade, dica);

        montador = () -> new Cartao(total, cbModalidade.getValue(), cbParcelas.getValue());
    }
}
