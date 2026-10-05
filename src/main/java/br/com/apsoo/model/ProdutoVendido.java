package br.com.apsoo.model;

// Produto com a quantidade vendida em um período
public record ProdutoVendido(String nome, int quantidade) {

    @Override
    public String toString() {
        return nome + " (" + quantidade + ")";
    }
}
