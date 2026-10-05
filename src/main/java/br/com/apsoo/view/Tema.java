package br.com.apsoo.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

// Estilo e formatação compartilhados pelas telas
public final class Tema {

    private static final String CSS = Tema.class.getResource("/css/tema.css").toExternalForm();
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Tema() {
    }

    public static void aplicar(Scene cena) {
        cena.getStylesheets().add(CSS);
    }

    public static void aplicar(DialogPane painel) {
        painel.getStylesheets().add(CSS);
    }

    public static String dataHora(LocalDateTime dataHora) {
        return DATA_HORA.format(dataHora);
    }

    public static String moeda(BigDecimal valor) {
        return MOEDA.format(valor);
    }

    // Aceita vírgula ou ponto como separador decimal; lança NumberFormatException se inválido
    public static BigDecimal lerValor(String texto) {
        BigDecimal valor = new BigDecimal(texto.trim().replace(',', '.'));
        if (valor.signum() < 0) {
            throw new NumberFormatException("valor negativo");
        }
        return valor;
    }
}
