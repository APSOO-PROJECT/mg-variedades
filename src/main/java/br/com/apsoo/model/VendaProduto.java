package br.com.apsoo.model;

// Quantidade vendida de um produto em um período, com a sua categoria (0 = sem venda)
public record VendaProduto(String categoria, String produto, int quantidade) {
}
