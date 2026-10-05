package br.com.apsoo.model;

import java.math.BigDecimal;

// Total vendido e quantidade de vendas de um vendedor em um período (RF21)
public record VendasVendedor(String vendedor, int quantidadeVendas, BigDecimal valorTotal) {
}
