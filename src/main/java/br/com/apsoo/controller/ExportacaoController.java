package br.com.apsoo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import br.com.apsoo.exportacao.ExportadorCsv;
import br.com.apsoo.exportacao.ExportadorPdf;
import br.com.apsoo.model.RelatorioExportavel;

// Caso de uso Exportar Relatório (RF23): somente administrador
public class ExportacaoController {

    // Grava o relatório (com os filtros já aplicados na consulta) e devolve o arquivo criado
    public File exportar(RelatorioExportavel relatorio, FormatoExportacao formato, File destino)
            throws RegraNegocioException, IOException {
        Sessao.exigirAdmin();
        if (relatorio == null) {
            throw new RegraNegocioException("Gere o relatório antes de exportar.");
        }

        relatorio.setGeradoEm(LocalDateTime.now());
        relatorio.setGeradoPor(Sessao.getUsuarioLogado().getNome());

        File arquivo = comExtensao(destino, formato);
        Path caminho = arquivo.toPath();
        switch (formato) {
            case PDF -> ExportadorPdf.exportar(relatorio, caminho);
            case CSV -> ExportadorCsv.exportar(relatorio, caminho);
        }
        return arquivo;
    }

    private File comExtensao(File destino, FormatoExportacao formato) {
        String sufixo = "." + formato.getExtensao();
        if (destino.getName().toLowerCase().endsWith(sufixo)) {
            return destino;
        }
        return new File(destino.getParentFile(), destino.getName() + sufixo);
    }
}
