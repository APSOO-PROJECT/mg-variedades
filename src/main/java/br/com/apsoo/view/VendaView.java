package br.com.apsoo.view;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import br.com.apsoo.controller.ProdutoController;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.VendaController;
import br.com.apsoo.model.Cartao;
import br.com.apsoo.model.CpfCnpj;
import br.com.apsoo.model.Dinheiro;
import br.com.apsoo.model.ItemVenda;
import br.com.apsoo.model.ModalidadeCartao;
import br.com.apsoo.model.Pagamento;
import br.com.apsoo.model.Pix;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.Venda;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// Registrar Venda em passos, como em um ponto de venda (PDV): Cliente -> Produtos -> Pagamento -> Revisão.
// Segue o fluxo do caso de uso Realizar Venda; os fluxos alternativos voltam ao passo indicado nele.
public class VendaView extends VBox {

    private static final int CLIENTE = 0;
    private static final int PRODUTOS = 1;
    private static final int PAGAMENTO = 2;
    private static final int REVISAO = 3;

    private final VendaController vendaController = new VendaController();
    private final ProdutoController produtoController = new ProdutoController();

    private final EtapasView etapas = new EtapasView("Cliente", "Produtos", "Pagamento", "Revisão");
    private final VBox area = new VBox();
    private final Label lblTotal = new Label();
    private final Button btnCancelar = Botoes.comIcone("Cancelar venda", "fechar", "danger");
    private final Button btnVoltar = Botoes.comIcone("Voltar", "menos");
    private final Button btnAvancar = new Button();

    // Passo 1
    private final TextField txtCpfCnpj = new TextField();
    private final Node passoCliente;

    // Passo 2
    private final TextField txtProduto = new TextField();
    private final Passo passoQuantidade = new Passo(1, 9999, 1);
    private final TableView<ItemVenda> tabelaItens = new TableView<>();
    private final ObservableList<ItemVenda> itens = FXCollections.observableArrayList();
    private final ContextMenu sugestoes = new ContextMenu();
    private final PauseTransition atrasoBusca = new PauseTransition(Duration.millis(250));
    private final Node passoProdutos;
    private Produto produtoSelecionado;

    // Passo 3
    private PainelPagamento painelPagamento;
    private BigDecimal totalDoPainel;
    private Node passoPagamento;
    private Pagamento pagamento;

    private int etapa = CLIENTE;

    public VendaView() {
        setSpacing(18);

        etapas.setAoEscolher(this::voltarPara);
        passoCliente = montarPassoCliente();
        passoProdutos = montarPassoProdutos();
        VBox.setVgrow(area, Priority.ALWAYS);
        area.setAlignment(Pos.TOP_CENTER); // os cartões ficam centralizados na área do passo

        getChildren().addAll(etapas, area, montarRodape());
        configurarBuscaDeProduto();

        // Ctrl+Enter avança de qualquer ponto da tela, para agilizar o atendimento
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && e.isControlDown() && btnAvancar.isVisible()) {
                e.consume();
                avancar();
            }
        });

        mostrar(CLIENTE);
    }

    // ------------------------------------------------------------------ montagem dos passos

    private Node montarPassoCliente() {
        txtCpfCnpj.getStyleClass().add("grande");
        txtCpfCnpj.setPromptText("000.000.000-00 ou 00.000.000/0000-00");
        txtCpfCnpj.setOnAction(e -> avancar());

        Label titulo = new Label("Quem é o cliente?");
        titulo.getStyleClass().add("subtitle");
        Label descricao = new Label("Informe o CPF ou o CNPJ para identificar a venda.");
        descricao.getStyleClass().add("muted");

        VBox card = new VBox(18, new VBox(4, titulo, descricao), new Campo("CPF/CNPJ *", txtCpfCnpj));
        card.getStyleClass().add("card");
        card.setMaxWidth(640);
        return card;
    }

    private Node montarPassoProdutos() {
        txtProduto.getStyleClass().add("grande");
        txtProduto.setPromptText("Digite o nome ou passe o código de barras");

        Button btnAdicionar = Botoes.comIcone("Adicionar", "mais", "accent");
        btnAdicionar.setOnAction(e -> adicionarProduto());
        HBox linhaProduto = new HBox(8, txtProduto, btnAdicionar);
        HBox.setHgrow(txtProduto, Priority.ALWAYS);

        Campo campoProduto = new Campo("Produto *", linhaProduto);
        Campo campoQuantidade = new Campo("Quantidade *", passoQuantidade);
        GradeResponsiva.ocupar(campoProduto, 2);
        GradeResponsiva grade = new GradeResponsiva(250, 16);
        grade.getChildren().addAll(campoProduto, campoQuantidade);

        Label titulo = new Label("Quais produtos o cliente vai levar?");
        titulo.getStyleClass().add("subtitle");

        VBox card = new VBox(16, titulo, grade, montarTabelaItens());
        card.getStyleClass().add("card");
        VBox.setVgrow(tabelaItens, Priority.ALWAYS);
        return card;
    }

    private TableView<ItemVenda> montarTabelaItens() {
        TableColumn<ItemVenda, Void> colRemover = new TableColumn<>("");
        colRemover.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                ItemVenda item = getTableView().getItems().get(getIndex());
                setGraphic(Botoes.acaoPerigosa("excluir", "Remover item", () -> removerItem(item)));
            }
        });
        Tabelas.fixa(colRemover, 70);

        tabelaItens.getColumns().addAll(
                Celulas.<ItemVenda>comIcone("Produto", 320, i -> "produtos", i -> "",
                        i -> i.getProduto().getNome(), i -> i.getProduto().getCategoria().getNome()),
                Celulas.<ItemVenda>numerica("Qtd.", 80, i -> String.valueOf(i.getQuantidade())),
                Celulas.<ItemVenda>numerica("Preço unit.", 130, i -> Tema.moeda(i.getPrecoUnitario())),
                Celulas.<ItemVenda>destaque("Subtotal", 130, i -> Tema.moeda(i.calcularSubtotal())),
                colRemover);
        tabelaItens.setItems(itens);
        tabelaItens.setPlaceholder(Tabelas.vazio("Nenhum produto adicionado"));
        Tabelas.preparar(tabelaItens);
        return tabelaItens;
    }

    private Node montarRodape() {
        lblTotal.getStyleClass().add("total");

        btnCancelar.setOnAction(e -> cancelar());
        btnVoltar.setOnAction(e -> voltarPara(etapa - 1));
        btnAvancar.setOnAction(e -> avancar());
        btnAvancar.setMnemonicParsing(false);

        return new Barra().aEsquerda(lblTotal).aDireita(btnCancelar, btnVoltar, btnAvancar);
    }

    // ------------------------------------------------------------------ navegação

    private void mostrar(int novaEtapa) {
        etapa = novaEtapa;
        etapas.definir(etapa, false);

        Node passo = switch (etapa) {
            case CLIENTE -> passoCliente;
            case PRODUTOS -> passoProdutos;
            case PAGAMENTO -> montarPassoPagamento();
            default -> montarPassoRevisao();
        };
        area.getChildren().setAll(passo);
        VBox.setVgrow(passo, etapa == PRODUTOS ? Priority.ALWAYS : Priority.NEVER);

        btnVoltar.setVisible(etapa > CLIENTE);
        btnVoltar.setManaged(etapa > CLIENTE);
        lblTotal.setVisible(etapa >= PRODUTOS);
        lblTotal.setManaged(etapa >= PRODUTOS);
        btnAvancar.getStyleClass().removeAll("accent", "success");
        btnAvancar.getStyleClass().add(etapa == REVISAO ? "success" : "accent");
        btnAvancar.setGraphic(Icones.de(etapa == REVISAO ? "confirmar" : "ativar", 16));
        btnAvancar.setText(switch (etapa) {
            case CLIENTE -> "Continuar";
            case PRODUTOS -> "Ir para o pagamento";
            case PAGAMENTO -> "Revisar venda";
            default -> "Finalizar venda";
        });
        atualizarTotal();

        Platform.runLater(() -> {
            if (etapa == CLIENTE) {
                txtCpfCnpj.requestFocus();
            } else if (etapa == PRODUTOS) {
                txtProduto.requestFocus();
            }
        });
    }

    // O vendedor só pode voltar para passos já feitos (pelo botão ou clicando no indicador de passos)
    private void voltarPara(int destino) {
        if (destino >= 0 && destino < etapa) {
            mostrar(destino);
        }
    }

    private void avancar() {
        try {
            switch (etapa) {
                case CLIENTE -> {
                    vendaController.identificarCliente(txtCpfCnpj.getText());
                    mostrar(PRODUTOS);
                }
                case PRODUTOS -> {
                    if (itens.isEmpty()) {
                        throw new RegraNegocioException("Adicione ao menos um produto à venda.");
                    }
                    mostrar(PAGAMENTO);
                }
                case PAGAMENTO -> {
                    pagamento = painelPagamento == null ? null : painelPagamento.criar();
                    if (pagamento == null) {
                        throw new IllegalArgumentException("Método de pagamento não preenchido");
                    }
                    mostrar(REVISAO);
                }
                default -> finalizar();
            }
        } catch (RegraNegocioException | IllegalArgumentException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void finalizar() throws RegraNegocioException, SQLException {
        int numero = vendaController.registrarVenda(pagamento);
        mostrarConclusao(numero, pagamento);
    }

    private void cancelar() {
        boolean haProgresso = !txtCpfCnpj.getText().isBlank() || !itens.isEmpty();
        if (haProgresso && !Dialogos.confirmar("Cancelar a venda?")) {
            return;
        }
        reiniciar();
    }

    private void reiniciar() {
        vendaController.cancelarVenda();
        itens.clear();
        txtCpfCnpj.clear();
        txtProduto.clear();
        produtoSelecionado = null;
        passoQuantidade.setValor(1);
        painelPagamento = null;
        totalDoPainel = null;
        passoPagamento = null;
        pagamento = null;
        setBotoesVisiveis(true);
        mostrar(CLIENTE);
    }

    private void setBotoesVisiveis(boolean visivel) {
        for (Node botao : List.of(btnCancelar, btnAvancar)) {
            botao.setVisible(visivel);
            botao.setManaged(visivel);
        }
        lblTotal.setVisible(visivel);
        lblTotal.setManaged(visivel);
        btnVoltar.setVisible(visivel && etapa > CLIENTE);
        btnVoltar.setManaged(visivel && etapa > CLIENTE);
    }

    // ------------------------------------------------------------------ passo Pagamento

    // O painel é refeito quando o total muda (o vendedor voltou e alterou os produtos)
    private Node montarPassoPagamento() {
        BigDecimal total = vendaController.calcularTotal();
        if (painelPagamento == null || totalDoPainel == null || totalDoPainel.compareTo(total) != 0) {
            painelPagamento = new PainelPagamento(total, this::avancar);
            totalDoPainel = total;

            Label titulo = new Label("Como o cliente vai pagar?");
            titulo.getStyleClass().add("subtitle");
            Label valor = new Label("Total a pagar: " + Tema.moeda(total));
            valor.getStyleClass().add("muted");
            VBox card = new VBox(18, new VBox(4, titulo, valor), painelPagamento);
            card.getStyleClass().add("card");
            card.setMaxWidth(820);
            passoPagamento = card;
        }
        return passoPagamento;
    }

    // ------------------------------------------------------------------ passo Revisão e conclusão

    private Node montarPassoRevisao() {
        Venda venda = vendaController.getVenda();

        Label titulo = new Label("Revise a venda");
        titulo.getStyleClass().add("subtitle");

        GradeResponsiva dados = new GradeResponsiva(240, 16);
        dados.getChildren().addAll(
                new Indicador("Cliente", CpfCnpj.formatar(venda.getCpfCnpjCliente())).comIcone("conta", ""),
                new Indicador("Pagamento", descreverPagamento(pagamento)).comIcone("cartao", "warning"));

        VBox lista = new VBox();
        for (ItemVenda item : venda.getItens()) {
            Label quantidade = new Label(item.getQuantidade() + " ×");
            quantidade.getStyleClass().addAll("chip", "cor-0");
            Label nome = new Label(item.getProduto().getNome());
            nome.getStyleClass().add("celula-nome");
            Label subtotal = new Label(Tema.moeda(item.calcularSubtotal()));
            subtotal.getStyleClass().add("celula-nome");
            Region folga = new Region();
            HBox.setHgrow(folga, Priority.ALWAYS);
            HBox linha = new HBox(12, quantidade, nome, folga, subtotal);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.getStyleClass().add("linha-resumo");
            lista.getChildren().add(linha);
        }

        VBox card = new VBox(18, titulo, dados, lista);
        card.getStyleClass().add("card");
        card.setMaxWidth(820);
        return card;
    }

    private String descreverPagamento(Pagamento p) {
        if (p instanceof Dinheiro dinheiro) {
            return "Dinheiro - recebido " + Tema.moeda(dinheiro.getValorRecebido())
                    + ", troco " + Tema.moeda(dinheiro.calcularTroco());
        }
        if (p instanceof Pix pix) {
            return "Pix - código " + pix.getComprovante();
        }
        Cartao cartao = (Cartao) p;
        return "Cartão de " + cartao.getModalidade().toString().toLowerCase()
                + (cartao.getModalidade() == ModalidadeCartao.CREDITO ? " em " + cartao.getParcelas() + "x" : "");
    }

    // Tela final: venda concluída (e o troco a entregar, no pagamento em dinheiro)
    private void mostrarConclusao(int numero, Pagamento paga) {
        etapas.definir(REVISAO, true);
        setBotoesVisiveis(false);

        StackPane quadro = new StackPane(Icones.de("confirmar", 40));
        quadro.getStyleClass().addAll("icon-chip", "success", "grande");
        quadro.setMaxSize(80, 80); // senão o VBox esticaria a bolha na largura toda
        Label titulo = new Label("Compra realizada com sucesso!");
        titulo.getStyleClass().add("title");
        Label venda = new Label("Venda nº " + numero + " - " + Tema.moeda(paga.getValorVenda()));
        venda.getStyleClass().add("muted");

        VBox conteudo = new VBox(14, quadro, titulo, venda);
        conteudo.setAlignment(Pos.CENTER);

        if (paga instanceof Dinheiro dinheiro) {
            Label troco = new Label("Troco a entregar: " + Tema.moeda(dinheiro.calcularTroco()));
            troco.getStyleClass().addAll("badge", "warning", "troco-final");
            conteudo.getChildren().add(troco);
        } else if (paga instanceof Pix) {
            conteudo.getChildren().add(rotuloConfirmacao("Pagamento Confirmado"));
        } else {
            conteudo.getChildren().add(rotuloConfirmacao("Pagamento Aprovado"));
        }

        Button btnNova = Botoes.comIcone("Nova venda", "mais", "accent");
        btnNova.setOnAction(e -> reiniciar());
        conteudo.getChildren().add(btnNova);
        VBox.setMargin(btnNova, new Insets(10, 0, 0, 0));

        VBox card = new VBox(conteudo);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(640);
        card.setPadding(new Insets(40));
        area.getChildren().setAll(card);
        VBox.setVgrow(card, Priority.NEVER);
        Platform.runLater(btnNova::requestFocus);
    }

    private Label rotuloConfirmacao(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().addAll("badge", "success");
        return rotulo;
    }

    // ------------------------------------------------------------------ passo Produtos: busca e itens

    private void configurarBuscaDeProduto() {
        // Autocompletar: consulta o banco 250 ms depois da última tecla digitada
        atrasoBusca.setOnFinished(e -> mostrarSugestoes());
        txtProduto.textProperty().addListener((obs, antigo, novo) -> {
            if (produtoSelecionado != null && !novo.equals(produtoSelecionado.getNome())) {
                produtoSelecionado = null;
            }
            if (produtoSelecionado == null) {
                atrasoBusca.playFromStart();
            }
        });
        // Enter adiciona o produto (o leitor de código de barras digita o código e envia Enter)
        txtProduto.setOnAction(e -> adicionarProduto());
    }

    private void mostrarSugestoes() {
        String termo = txtProduto.getText();
        if (produtoSelecionado != null || termo.isBlank() || !txtProduto.isFocused()) {
            sugestoes.hide();
            return;
        }
        try {
            List<Produto> encontrados = produtoController.buscar(termo);
            sugestoes.getItems().clear();
            for (Produto produto : encontrados) {
                Label nome = new Label(produto.getNome());
                nome.getStyleClass().add("field-label");
                Label detalhe = new Label(Tema.moeda(produto.getPreco()) + "  -  estoque: "
                        + produto.getQuantidadeAtual());
                detalhe.getStyleClass().addAll("muted", "hint");
                VBox conteudo = new VBox(1, nome, detalhe);
                conteudo.setPrefWidth(Math.max(260, txtProduto.getWidth() - 24));

                CustomMenuItem item = new CustomMenuItem(conteudo, true);
                item.setOnAction(e -> selecionarProduto(produto));
                sugestoes.getItems().add(item);
            }
            if (encontrados.isEmpty()) {
                sugestoes.hide();
            } else if (!sugestoes.isShowing()) {
                sugestoes.show(txtProduto, Side.BOTTOM, 0, 4);
            }
        } catch (SQLException ex) {
            sugestoes.hide();
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void selecionarProduto(Produto produto) {
        produtoSelecionado = produto;
        txtProduto.setText(produto.getNome());
        txtProduto.positionCaret(txtProduto.getText().length());
        sugestoes.hide();
    }

    // Produto não encontrado ou quantidade acima do estoque: a mensagem aparece e o vendedor continua neste passo
    private void adicionarProduto() {
        sugestoes.hide();
        try {
            Produto produto = produtoSelecionado != null ? produtoSelecionado : localizarProduto();
            vendaController.adicionarItem(produto, passoQuantidade.getValor());

            atualizarItens();
            produtoSelecionado = null;
            txtProduto.clear();
            passoQuantidade.setValor(1);
            txtProduto.requestFocus();
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    // Quando o vendedor digita e confirma sem escolher uma sugestão
    private Produto localizarProduto() throws SQLException {
        List<Produto> encontrados = produtoController.buscar(txtProduto.getText());
        String digitado = txtProduto.getText().trim();
        for (Produto produto : encontrados) {
            if (produto.getNome().equalsIgnoreCase(digitado) || digitado.equals(produto.getCodigoBarras())) {
                return produto;
            }
        }
        return encontrados.size() == 1 ? encontrados.get(0) : null;
    }

    private void removerItem(ItemVenda item) {
        vendaController.removerItem(item);
        atualizarItens();
    }

    private void atualizarItens() {
        itens.setAll(vendaController.getVenda().getItens());
        atualizarTotal();
    }

    private void atualizarTotal() {
        lblTotal.setText("Total: " + Tema.moeda(vendaController.calcularTotal()));
    }
}
