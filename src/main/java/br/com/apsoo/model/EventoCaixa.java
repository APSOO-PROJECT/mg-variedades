package br.com.apsoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Linha do histórico de caixa (abertura, fechamento, sangria, suprimento ou reembolso); somente leitura
public class EventoCaixa {
    private final LocalDateTime dataHora;
    private final String evento;
    private final BigDecimal valor;
    private final String detalhe;
    private final String usuarioNome;
    private final int caixaId;
    private BigDecimal valorEsperado; // preenchido apenas nos fechamentos

    public EventoCaixa(LocalDateTime dataHora, String evento, BigDecimal valor, String detalhe,
                       String usuarioNome, int caixaId) {
        this.dataHora = dataHora;
        this.evento = evento;
        this.valor = valor;
        this.detalhe = detalhe;
        this.usuarioNome = usuarioNome;
        this.caixaId = caixaId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getEvento() {
        return evento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public int getCaixaId() {
        return caixaId;
    }

    public BigDecimal getValorEsperado() {
        return valorEsperado;
    }

    public void setValorEsperado(BigDecimal valorEsperado) {
        this.valorEsperado = valorEsperado;
    }
}
