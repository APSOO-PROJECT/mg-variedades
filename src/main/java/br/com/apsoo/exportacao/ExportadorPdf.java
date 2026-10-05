package br.com.apsoo.exportacao;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.apsoo.model.RelatorioExportavel;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.HeaderFooter;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

// Gera o relatório em PDF (A4; paisagem quando alguma tabela tem mais de 5 colunas)
public final class ExportadorPdf {

    private static final int COLUNAS_MAXIMAS_RETRATO = 5;
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COR_CABECALHO = new Color(0xEA, 0xEE, 0xF2);
    private static final Color COR_BORDA = new Color(0xD0, 0xD7, 0xDE);

    private static final Font FONTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
    private static final Font FONTE_SECAO = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
    private static final Font FONTE_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
    private static final Font FONTE_NEGRITO = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
    private static final Font FONTE_DISCRETA = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.GRAY);

    private ExportadorPdf() {
    }

    public static void exportar(RelatorioExportavel relatorio, Path destino) throws IOException {
        boolean paisagem = relatorio.getSecoes().stream()
                .anyMatch(s -> s.cabecalhos().size() > COLUNAS_MAXIMAS_RETRATO);
        Document documento = new Document(paisagem ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 36);

        try (OutputStream saida = Files.newOutputStream(destino)) {
            PdfWriter.getInstance(documento, saida);

            HeaderFooter rodape = new HeaderFooter(new Phrase("Página ", FONTE_DISCRETA), true);
            rodape.setBorder(Rectangle.NO_BORDER);
            rodape.setAlignment(Element.ALIGN_CENTER);
            documento.setFooter(rodape);

            documento.open();
            documento.add(new Paragraph(relatorio.getTitulo(), FONTE_TITULO));
            documento.add(new Paragraph("MG Variedades - gerado em " + relatorio.getGeradoEm().format(DATA_HORA)
                    + " por " + relatorio.getGeradoPor(), FONTE_DISCRETA));

            if (!relatorio.getFiltros().isEmpty()) {
                documento.add(espaco());
                relatorio.getFiltros().forEach((nome, valor) -> adicionar(documento,
                        paragrafoRotulado(nome, valor)));
            }
            if (!relatorio.getResumo().isEmpty()) {
                documento.add(espaco());
                documento.add(tabelaResumo(relatorio));
            }
            for (RelatorioExportavel.Secao secao : relatorio.getSecoes()) {
                documento.add(espaco());
                Paragraph titulo = new Paragraph(secao.titulo(), FONTE_SECAO);
                titulo.setSpacingAfter(4);
                documento.add(titulo);
                documento.add(tabela(secao));
            }
            // Precisa fechar antes de o arquivo ser fechado pelo try-with-resources
            documento.close();
        } catch (DocumentException e) {
            throw new IOException("Falha ao montar o PDF: " + e.getMessage(), e);
        }
    }

    private static Paragraph espaco() {
        Paragraph espaco = new Paragraph(" ", FONTE_DISCRETA);
        espaco.setLeading(8);
        return espaco;
    }

    private static Paragraph paragrafoRotulado(String nome, String valor) {
        Paragraph paragrafo = new Paragraph();
        paragrafo.add(new Phrase(nome + ": ", FONTE_NEGRITO));
        paragrafo.add(new Phrase(valor, FONTE_NORMAL));
        return paragrafo;
    }

    // Lambdas não podem lançar DocumentException
    private static void adicionar(Document documento, Paragraph paragrafo) {
        try {
            documento.add(paragrafo);
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PdfPTable tabelaResumo(RelatorioExportavel relatorio) {
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(60);
        tabela.setHorizontalAlignment(Element.ALIGN_LEFT);
        relatorio.getResumo().forEach((nome, valor) -> {
            tabela.addCell(celula(nome, FONTE_NEGRITO, null));
            tabela.addCell(celula(valor, FONTE_NORMAL, null));
        });
        return tabela;
    }

    private static PdfPTable tabela(RelatorioExportavel.Secao secao) {
        PdfPTable tabela = new PdfPTable(secao.cabecalhos().size());
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);

        for (String cabecalho : secao.cabecalhos()) {
            tabela.addCell(celula(cabecalho, FONTE_NEGRITO, COR_CABECALHO));
        }
        if (secao.linhas().isEmpty()) {
            PdfPCell vazio = celula("Nenhum registro", FONTE_DISCRETA, null);
            vazio.setColspan(secao.cabecalhos().size());
            tabela.addCell(vazio);
            return tabela;
        }
        for (List<String> linha : secao.linhas()) {
            for (String valor : linha) {
                tabela.addCell(celula(valor, FONTE_NORMAL, null));
            }
        }
        return tabela;
    }

    private static PdfPCell celula(String texto, Font fonte, Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto == null ? "" : texto, fonte));
        celula.setPadding(5);
        celula.setBorderColor(COR_BORDA);
        if (fundo != null) {
            celula.setBackgroundColor(fundo);
        }
        return celula;
    }
}
