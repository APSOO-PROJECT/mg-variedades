package br.com.apsoo.view;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Células decoradas para as tabelas: posição em rankings, barras de progresso, avatares, chips e selos
public final class Celulas {

    private static final int CORES_DA_PALETA = 6;

    // Cria o conteúdo gráfico de uma linha; "todos" permite comparar com as demais (por exemplo, a barra
    // é proporcional ao maior valor da tabela)
    @FunctionalInterface
    private interface Montador<T> {
        Node montar(T item, int indice, List<T> todos);
    }

    private Celulas() {
    }

    // ------------------------------------------------------------------ texto

    // Texto alinhado à direita (valores e quantidades)
    public static <T> TableColumn<T, String> numerica(String titulo, double largura, Function<T, String> valor) {
        TableColumn<T, String> coluna = Tabelas.coluna(titulo, largura, valor);
        coluna.getStyleClass().add("coluna-numerica");
        coluna.setMinWidth(largura); // números não encolhem, para não aparecerem cortados
        return coluna;
    }

    // Texto à direita e em negrito (totais e valores principais)
    public static <T> TableColumn<T, String> destaque(String titulo, double largura, Function<T, String> valor) {
        TableColumn<T, String> coluna = numerica(titulo, largura, valor);
        coluna.getStyleClass().add("coluna-destaque");
        return coluna;
    }

    // ------------------------------------------------------------------ ranking

    // Posição da linha na tabela (a ordem vem da consulta): 1º ouro, 2º prata, 3º bronze
    public static <T> TableColumn<T, Void> posicao(String titulo) {
        TableColumn<T, Void> coluna = colunaDe(titulo, 70, (item, indice, todos) -> distintivo(indice + 1));
        Tabelas.fixa(coluna, 70);
        return coluna;
    }


    private static Node distintivo(int posicao) {
        Label rotulo = new Label(String.valueOf(posicao));
        rotulo.getStyleClass().add("posicao");
        if (posicao <= 3) {
            rotulo.getStyleClass().add("p" + posicao);
        }
        return rotulo;
    }

    // ------------------------------------------------------------------ barra de progresso

    // Texto com uma barra abaixo, proporcional ao maior valor da tabela; estilo: "", "success", "warning" ou "danger"
    public static <T> TableColumn<T, Void> medidor(String titulo, double largura, ToDoubleFunction<T> valor,
                                                   Function<T, String> texto, Function<T, String> estilo) {
        TableColumn<T, Void> coluna = colunaDe(titulo, largura, (item, indice, todos) -> {
            double maximo = todos.stream().mapToDouble(valor).max().orElse(0);
            double fracao = maximo <= 0 ? 0 : valor.applyAsDouble(item) / maximo;
            return new Medidor(texto.apply(item), fracao, estilo.apply(item));
        });
        coluna.setMinWidth(largura * 0.85);
        return coluna;
    }

    public static <T> TableColumn<T, Void> medidor(String titulo, double largura, ToDoubleFunction<T> valor,
                                                   Function<T, String> texto) {
        return medidor(titulo, largura, valor, texto, item -> "");
    }

    // ------------------------------------------------------------------ pessoas, chips e selos

    // Avatar com as iniciais, nome em destaque e um detalhe discreto abaixo
    public static <T> TableColumn<T, Void> pessoa(String titulo, double largura, Function<T, String> nome,
                                                  Function<T, String> detalhe) {
        return colunaDe(titulo, largura, (item, indice, todos) -> {
            String nomeCompleto = nome.apply(item);
            Label lblNome = new Label(nomeCompleto);
            lblNome.getStyleClass().add("celula-nome");
            VBox textos = new VBox(1, lblNome);
            textos.setAlignment(Pos.CENTER_LEFT);
            String complemento = detalhe == null ? null : detalhe.apply(item);
            if (complemento != null && !complemento.isBlank()) {
                Label lblDetalhe = new Label(complemento);
                lblDetalhe.getStyleClass().addAll("muted", "hint");
                textos.getChildren().add(lblDetalhe);
            }
            HBox linha = new HBox(10, avatar(nomeCompleto), textos);
            linha.setFillHeight(false); // o avatar mantém o formato redondo
            linha.setAlignment(Pos.CENTER_LEFT);
            return linha;
        });
    }

    // Ícone em quadro colorido + nome (produto, evento...); estilo do quadro: "", "success", "danger", "warning"
    public static <T> TableColumn<T, Void> comIcone(String titulo, double largura, Function<T, String> icone,
                                                    Function<T, String> estilo, Function<T, String> nome,
                                                    Function<T, String> detalhe) {
        return colunaDe(titulo, largura, (item, indice, todos) -> {
            StackPane quadro = new StackPane(Icones.de(icone.apply(item), 16));
            quadro.getStyleClass().addAll("icon-chip", "mini");
            String cor = estilo.apply(item);
            if (!cor.isEmpty()) {
                quadro.getStyleClass().add(cor);
            }
            Label lblNome = new Label(nome.apply(item));
            lblNome.getStyleClass().add("celula-nome");
            VBox textos = new VBox(1, lblNome);
            textos.setAlignment(Pos.CENTER_LEFT);
            String complemento = detalhe == null ? null : detalhe.apply(item);
            if (complemento != null && !complemento.isBlank()) {
                Label lblDetalhe = new Label(complemento);
                lblDetalhe.getStyleClass().addAll("muted", "hint");
                textos.getChildren().add(lblDetalhe);
            }
            HBox linha = new HBox(10, quadro, textos);
            linha.setFillHeight(false); // o ícone mantém o formato quadrado
            linha.setAlignment(Pos.CENTER_LEFT);
            return linha;
        });
    }

    // Chip colorido (a cor é sempre a mesma para o mesmo nome)
    public static <T> TableColumn<T, Void> chip(String titulo, double largura, Function<T, String> nome) {
        TableColumn<T, Void> coluna = colunaDe(titulo, largura, (item, indice, todos) -> {
            String texto = nome.apply(item);
            Label chip = new Label(texto);
            chip.getStyleClass().addAll("chip", "cor-" + corDe(texto));
            return chip;
        });
        coluna.setMinWidth(largura);
        return coluna;
    }

    // Selo de estado; estilo: "success", "danger", "warning" ou "neutral"
    public static <T> TableColumn<T, Void> selo(String titulo, double largura, Function<T, String> texto,
                                                Function<T, String> estilo) {
        TableColumn<T, Void> coluna = colunaDe(titulo, largura, (item, indice, todos) -> {
            String conteudo = texto.apply(item);
            if (conteudo == null || conteudo.isEmpty()) {
                return new Region();
            }
            Label selo = new Label(conteudo);
            selo.getStyleClass().addAll("badge", estilo.apply(item));
            return selo;
        });
        coluna.setMinWidth(largura);
        return coluna;
    }

    // ------------------------------------------------------------------ base

    private static <T> TableColumn<T, Void> colunaDe(String titulo, double largura, Montador<T> montador) {
        TableColumn<T, Void> coluna = new TableColumn<>(titulo);
        coluna.setPrefWidth(largura);
        coluna.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                int indice = getIndex();
                List<T> itens = getTableView() == null ? List.of() : getTableView().getItems();
                if (empty || indice < 0 || indice >= itens.size()) {
                    setGraphic(null);
                    return;
                }
                setGraphic(montador.montar(itens.get(indice), indice, itens));
            }
        });
        return coluna;
    }

    // Círculo com as iniciais do nome, em uma das cores da paleta
    private static Node avatar(String nome) {
        // Ignora complementos entre parênteses, como "(padrão)", ao tirar as iniciais
        String[] partes = nome.replaceAll("\\s*\\(.*?\\)", "").trim().split("\\s+");
        String iniciais = partes[0].substring(0, 1)
                + (partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "");
        Label texto = new Label(iniciais.toUpperCase());
        StackPane circulo = new StackPane(texto);
        circulo.getStyleClass().addAll("avatar-mini", "cor-" + corDe(nome));
        return circulo;
    }

    private static int corDe(String texto) {
        return Math.floorMod(texto.toLowerCase().hashCode(), CORES_DA_PALETA);
    }

    // Valor em texto e, abaixo, uma barra arredondada preenchida na proporção indicada
    private static final class Medidor extends VBox {

        Medidor(String texto, double fracao, String estilo) {
            super(5);
            setAlignment(Pos.CENTER_LEFT);
            Label rotulo = new Label(texto);
            rotulo.getStyleClass().add("medidor-texto");
            getChildren().addAll(rotulo, new Trilho(fracao, estilo));
        }
    }

    private static final class Trilho extends Pane {
        private static final double ALTURA = 7;

        private final Region preenchimento = new Region();
        private final double fracao;

        Trilho(double fracao, String estilo) {
            this.fracao = Math.max(0, Math.min(1, fracao));
            getStyleClass().add("medidor-trilho");
            preenchimento.getStyleClass().add("medidor-barra");
            if (!estilo.isEmpty()) {
                preenchimento.getStyleClass().add(estilo);
            }
            getChildren().add(preenchimento);
            setMinHeight(ALTURA);
            setPrefHeight(ALTURA);
            setMaxHeight(ALTURA);
            setPrefWidth(100);
        }

        @Override
        protected void layoutChildren() {
            // Mesmo valores pequenos mostram um pedaço da barra, para não parecer vazio
            double largura = fracao <= 0 ? 0 : Math.max(ALTURA, getWidth() * fracao);
            preenchimento.resizeRelocate(0, 0, largura, getHeight());
        }
    }
}
