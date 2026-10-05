package br.com.apsoo.view;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.function.Supplier;

import br.com.apsoo.controller.ExportacaoController;
import br.com.apsoo.controller.FormatoExportacao;
import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.model.RelatorioExportavel;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

// Botão "Exportar" dos relatórios (RF23): grava o resultado exibido na tela em PDF ou CSV
public class BotaoExportar extends MenuButton {

    private static File ultimaPasta;

    private final Supplier<RelatorioExportavel> relatorio;
    private final ExportacaoController controller = new ExportacaoController();

    // O fornecedor devolve o relatório da última consulta feita na tela (null se ainda não houve)
    public BotaoExportar(Supplier<RelatorioExportavel> relatorio) {
        super("Exportar");
        setGraphic(Icones.de("exportar", 16));
        setMnemonicParsing(false);
        this.relatorio = relatorio;

        for (FormatoExportacao formato : FormatoExportacao.values()) {
            MenuItem item = new MenuItem(formato.toString());
            item.setOnAction(e -> exportar(formato));
            getItems().add(item);
        }
    }

    // Barra com o filtro à esquerda e o botão à direita (desce para outra linha em janelas estreitas)
    public Barra barraCom(Node filtro) {
        return new Barra().aEsquerda(filtro).aDireita(this);
    }

    private void exportar(FormatoExportacao formato) {
        RelatorioExportavel conteudo = relatorio.get();
        if (conteudo == null) {
            Dialogos.avisar("Gere o relatório antes de exportar.");
            return;
        }

        FileChooser seletor = new FileChooser();
        seletor.setTitle("Exportar relatório");
        seletor.setInitialFileName(nomeSugerido(conteudo.getTitulo()) + "." + formato.getExtensao());
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                formato.toString(), "*." + formato.getExtensao()));
        if (ultimaPasta != null && ultimaPasta.isDirectory()) {
            seletor.setInitialDirectory(ultimaPasta);
        }

        File destino = seletor.showSaveDialog(getScene().getWindow());
        if (destino == null) {
            return;
        }

        try {
            File arquivo = controller.exportar(conteudo, formato, destino);
            ultimaPasta = arquivo.getParentFile();
            Dialogos.informar("Relatório exportado com sucesso:\n" + arquivo.getAbsolutePath());
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (IOException ex) {
            Dialogos.erro("Não foi possível gravar o arquivo: " + ex.getMessage());
        }
    }

    // "Relatório de vendas por período" -> "relatorio-de-vendas-por-periodo-2026-10-05"
    private String nomeSugerido(String titulo) {
        String base = Normalizer.normalize(titulo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return base + "-" + LocalDate.now();
    }
}
