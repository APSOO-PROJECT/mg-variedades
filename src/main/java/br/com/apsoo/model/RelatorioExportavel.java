package br.com.apsoo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Conteúdo de um relatório pronto para exportação (RF23): título, filtros aplicados, resumo e tabelas.
// Os textos já vêm formatados como aparecem na tela.
public class RelatorioExportavel {

    // Uma tabela do relatório
    public record Secao(String titulo, List<String> cabecalhos, List<List<String>> linhas) {
    }

    private final String titulo;
    private final Map<String, String> filtros = new LinkedHashMap<>();
    private final Map<String, String> resumo = new LinkedHashMap<>();
    private final List<Secao> secoes = new ArrayList<>();
    private LocalDateTime geradoEm;
    private String geradoPor;

    public RelatorioExportavel(String titulo) {
        this.titulo = titulo;
    }

    public void adicionarFiltro(String nome, String valor) {
        filtros.put(nome, valor);
    }

    public void adicionarResumo(String nome, String valor) {
        resumo.put(nome, valor);
    }

    public void adicionarSecao(String titulo, List<String> cabecalhos, List<List<String>> linhas) {
        secoes.add(new Secao(titulo, cabecalhos, linhas));
    }

    // Métodos Getters e Setters
    public String getTitulo() {
        return titulo;
    }

    public Map<String, String> getFiltros() {
        return filtros;
    }

    public Map<String, String> getResumo() {
        return resumo;
    }

    public List<Secao> getSecoes() {
        return secoes;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }
    public void setGeradoEm(LocalDateTime geradoEm) {
        this.geradoEm = geradoEm;
    }

    public String getGeradoPor() {
        return geradoPor;
    }
    public void setGeradoPor(String geradoPor) {
        this.geradoPor = geradoPor;
    }
}
