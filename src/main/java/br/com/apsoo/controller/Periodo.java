package br.com.apsoo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Filtro de período usado nas consultas e relatórios: dia, mês ou ano
public enum Periodo {
    DIA("Dia"), MES("Mês"), ANO("Ano");

    private final String descricao;

    Periodo(String descricao) {
        this.descricao = descricao;
    }

    // Primeiro dia do período que contém a data de referência
    public LocalDate inicio(LocalDate referencia) {
        return switch (this) {
            case DIA -> referencia;
            case MES -> referencia.withDayOfMonth(1);
            case ANO -> referencia.withDayOfYear(1);
        };
    }

    // Primeiro dia após o período (limite exclusivo)
    public LocalDate fim(LocalDate inicio) {
        return switch (this) {
            case DIA -> inicio.plusDays(1);
            case MES -> inicio.plusMonths(1);
            case ANO -> inicio.plusYears(1);
        };
    }

    // Texto do período que contém a data de referência (usado nos relatórios exportados)
    public String descrever(LocalDate referencia) {
        return switch (this) {
            case DIA -> "Dia " + referencia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case MES -> "Mês " + referencia.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            case ANO -> "Ano " + referencia.getYear();
        };
    }

    @Override
    public String toString() {
        return descricao;
    }
}
