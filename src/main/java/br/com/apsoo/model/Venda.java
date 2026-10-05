package br.com.apsoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venda {
    private Integer id;
    private Usuario usuario;
    private Caixa caixa;
    private String cpfCnpjCliente;
    private LocalDateTime dataHora;
    private final List<ItemVenda> itens = new ArrayList<>();
    private Pagamento pagamento;

    public Venda() {}

    public Venda(Usuario usuario, Caixa caixa, String cpfCnpjCliente) {
        this.usuario = usuario;
        this.caixa = caixa;
        this.cpfCnpjCliente = cpfCnpjCliente;
    }

    // Se o produto já está na venda, apenas soma a quantidade
    public void adicionarItem(Produto produto, int quantidade) {
        for (ItemVenda item : itens) {
            if (item.getProduto().getId().equals(produto.getId())) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return;
            }
        }
        itens.add(new ItemVenda(produto, quantidade));
    }

    public void removerItem(ItemVenda item) {
        itens.remove(item);
    }

    public int quantidadeDoProduto(Produto produto) {
        for (ItemVenda item : itens) {
            if (item.getProduto().getId().equals(produto.getId())) {
                return item.getQuantidade();
            }
        }
        return 0;
    }

    // Regra de Negócio: o prazo de devolução vem da categoria do produto
    public LocalDateTime prazoLimiteDevolucao(ItemVenda item) {
        return dataHora.plusHours(item.getProduto().getCategoria().getPrazoDevolucaoHoras());
    }

    public boolean isDentroDoPrazo(ItemVenda item, LocalDateTime agora) {
        return !agora.isAfter(prazoLimiteDevolucao(item));
    }

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda item : itens) {
            total = total.add(item.calcularSubtotal());
        }
        return total;
    }

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Caixa getCaixa() {
        return caixa;
    }
    public void setCaixa(Caixa caixa) {
        this.caixa = caixa;
    }

    public String getCpfCnpjCliente() {
        return cpfCnpjCliente;
    }
    public void setCpfCnpjCliente(String cpfCnpjCliente) {
        this.cpfCnpjCliente = cpfCnpjCliente;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }
    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }
}
