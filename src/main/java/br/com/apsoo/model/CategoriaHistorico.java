package br.com.apsoo.model;

import java.time.LocalDateTime;

// Registro imutável de uma alteração feita em uma categoria
public class CategoriaHistorico {
    private final LocalDateTime dataAlteracao;
    private final String campo;
    private final String valorAnterior;
    private final String valorNovo;
    private final String usuarioNome;

    public CategoriaHistorico(LocalDateTime dataAlteracao, String campo, String valorAnterior,
                              String valorNovo, String usuarioNome) {
        this.dataAlteracao = dataAlteracao;
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
        this.usuarioNome = usuarioNome;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public String getCampo() {
        return campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }
}
