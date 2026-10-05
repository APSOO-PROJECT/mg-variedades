package br.com.apsoo.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

// Logo da loja desenhada a partir do SVG (vetorial, nítida em qualquer tamanho e com a cor que se quiser):
// branco sobre áreas azuis ou escuras e azul da marca sobre áreas claras.
public final class Logo {

    public static final Color BRANCO = Color.WHITE;
    public static final Color AZUL = Color.web("#2563eb");

    // O SVG tem 3 caminhos, nesta ordem: letras "MG", carrinho (desenhado em traço) e letras "VARIEDADES"
    private static final List<String> CAMINHOS = carregarCaminhos();
    private static final int LETRAS_MG = 0;
    private static final int CARRINHO = 1;
    private static final int LETRAS_VARIEDADES = 2;
    private static final double ESPESSURA_TRACO = 27;

    // Áreas úteis dentro do desenho (viewBox 1001 x 460, que tem margens vazias em volta)
    private static final double[] AREA_COMPLETA = {0, 70, 945, 342};
    private static final double[] AREA_CARRINHO = {0, 70, 340, 326};

    private Logo() {
    }

    // Carrinho + "MG" + "VARIEDADES"
    public static Node completo(double largura, Color cor) {
        return montar(largura, AREA_COMPLETA, cor, true);
    }

    // Apenas o carrinho, para espaços pequenos
    public static Node icone(double largura, Color cor) {
        return montar(largura, AREA_CARRINHO, cor, false);
    }

    private static Node montar(double largura, double[] area, Color cor, boolean comLetras) {
        Group desenho = new Group();
        if (comLetras) {
            desenho.getChildren().add(letras(CAMINHOS.get(LETRAS_MG), cor));
            desenho.getChildren().add(letras(CAMINHOS.get(LETRAS_VARIEDADES), cor));
        }
        desenho.getChildren().add(carrinho(CAMINHOS.get(CARRINHO), cor));

        // Primeiro recorta a área útil (translação) e depois ajusta a escala à largura pedida
        double escala = largura / area[2];
        desenho.getTransforms().addAll(new Scale(escala, escala, 0, 0), new Translate(-area[0], -area[1]));

        Pane quadro = new Pane(desenho);
        double altura = area[3] * escala;
        quadro.setMinSize(largura, altura);
        quadro.setPrefSize(largura, altura);
        quadro.setMaxSize(largura, altura);
        quadro.setMouseTransparent(true);
        return quadro;
    }

    private static SVGPath letras(String caminho, Color cor) {
        SVGPath forma = new SVGPath();
        forma.setContent(caminho);
        forma.setFill(cor);
        return forma;
    }

    // O carrinho é um contorno (sem preenchimento) com pontas e quinas arredondadas
    private static SVGPath carrinho(String caminho, Color cor) {
        SVGPath forma = new SVGPath();
        forma.setContent(caminho);
        forma.setFill(null);
        forma.setStroke(cor);
        forma.setStrokeWidth(ESPESSURA_TRACO);
        forma.setStrokeLineCap(StrokeLineCap.ROUND);
        forma.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return forma;
    }

    private static List<String> carregarCaminhos() {
        try (InputStream entrada = Logo.class.getResourceAsStream("/images/mg_variedades_logo.svg")) {
            String svg = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            Matcher encontrado = Pattern.compile("<path\\s+d=\"([^\"]+)\"").matcher(svg);
            List<String> caminhos = new ArrayList<>();
            while (encontrado.find()) {
                caminhos.add(encontrado.group(1));
            }
            if (caminhos.size() < 3) {
                throw new IllegalStateException("O logo SVG deveria ter 3 caminhos, mas tem " + caminhos.size());
            }
            return caminhos;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
