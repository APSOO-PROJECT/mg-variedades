package br.com.apsoo.exportacao;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.apsoo.model.RelatorioExportavel;

// Gera planilha CSV (separador ponto e vírgula, UTF-8 com BOM) que o Excel abre diretamente
public final class ExportadorCsv {

    private static final char SEPARADOR = ';';
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExportadorCsv() {
    }

    public static void exportar(RelatorioExportavel relatorio, Path destino) throws IOException {
        try (OutputStream saida = Files.newOutputStream(destino);
             Writer escritor = new OutputStreamWriter(saida, StandardCharsets.UTF_8)) {
            escritor.write('﻿'); // BOM: faz o Excel reconhecer os acentos

            linha(escritor, relatorio.getTitulo());
            linha(escritor, "Gerado em", relatorio.getGeradoEm().format(DATA_HORA));
            linha(escritor, "Gerado por", relatorio.getGeradoPor());
            relatorio.getFiltros().forEach((nome, valor) -> linhaSemErro(escritor, nome, valor));
            linha(escritor);

            if (!relatorio.getResumo().isEmpty()) {
                relatorio.getResumo().forEach((nome, valor) -> linhaSemErro(escritor, nome, valor));
                linha(escritor);
            }

            for (RelatorioExportavel.Secao secao : relatorio.getSecoes()) {
                linha(escritor, secao.titulo());
                linha(escritor, secao.cabecalhos().toArray(String[]::new));
                for (List<String> linha : secao.linhas()) {
                    linha(escritor, linha.toArray(String[]::new));
                }
                linha(escritor);
            }
        }
    }

    private static void linha(Writer escritor, String... campos) throws IOException {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                texto.append(SEPARADOR);
            }
            texto.append(proteger(campos[i]));
        }
        escritor.write(texto.append("\r\n").toString());
    }

    // Auxiliar para uso dentro de lambdas, que não podem lançar IOException
    private static void linhaSemErro(Writer escritor, String... campos) {
        try {
            linha(escritor, campos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Aspas quando necessário e proteção contra interpretação como fórmula pelo Excel
    static String proteger(String valor) {
        String texto = valor == null ? "" : valor;
        if (iniciaFormula(texto)) {
            texto = "'" + texto;
        }
        if (texto.indexOf(SEPARADOR) >= 0 || texto.indexOf('"') >= 0
                || texto.indexOf('\n') >= 0 || texto.indexOf('\r') >= 0) {
            texto = "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    // Valores como "-R$ 10,00" ou "-5" são números negativos legítimos, não fórmulas
    private static boolean iniciaFormula(String texto) {
        if (texto.isEmpty()) {
            return false;
        }
        char primeiro = texto.charAt(0);
        if (primeiro == '=' || primeiro == '+' || primeiro == '@' || primeiro == '\t' || primeiro == '\r') {
            return true;
        }
        return primeiro == '-' && !texto.equals("-") && !texto.matches("-\\s?(R\\$\\s?)?[\\d.,]+");
    }
}
