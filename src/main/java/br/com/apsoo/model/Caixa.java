package br.com.apsoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Caixa {
    private Integer id;
    private BigDecimal fundoTrocoInicial;
    private LocalDateTime dataHoraAbertura;
    private LocalDateTime dataHoraFechamento;
    private BigDecimal saldoEsperado;
    private BigDecimal saldoReal;
    private String justificativaQuebra;
    private String usuarioAberturaNome;

    public Caixa() {}

    public Caixa(Integer id, BigDecimal fundoTrocoInicial, LocalDateTime dataHoraAbertura) {
        this.id = id;
        this.fundoTrocoInicial = fundoTrocoInicial;
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public boolean isAberto() {
        return dataHoraFechamento == null;
    }

    // Diferença entre o valor contado e o esperado (quebra de caixa)
    public BigDecimal calcularQuebra() {
        return saldoReal.subtract(saldoEsperado);
    }

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getFundoTrocoInicial() {
        return fundoTrocoInicial;
    }
    public void setFundoTrocoInicial(BigDecimal fundoTrocoInicial) {
        this.fundoTrocoInicial = fundoTrocoInicial;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }
    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public LocalDateTime getDataHoraFechamento() {
        return dataHoraFechamento;
    }
    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }

    public BigDecimal getSaldoEsperado() {
        return saldoEsperado;
    }
    public void setSaldoEsperado(BigDecimal saldoEsperado) {
        this.saldoEsperado = saldoEsperado;
    }

    public BigDecimal getSaldoReal() {
        return saldoReal;
    }
    public void setSaldoReal(BigDecimal saldoReal) {
        this.saldoReal = saldoReal;
    }

    public String getJustificativaQuebra() {
        return justificativaQuebra;
    }
    public void setJustificativaQuebra(String justificativaQuebra) {
        this.justificativaQuebra = justificativaQuebra;
    }

    public String getUsuarioAberturaNome() {
        return usuarioAberturaNome;
    }
    public void setUsuarioAberturaNome(String usuarioAberturaNome) {
        this.usuarioAberturaNome = usuarioAberturaNome;
    }
}
