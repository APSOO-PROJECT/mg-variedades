package br.com.apsoo.controller;

import java.math.BigDecimal;
import java.sql.SQLException;

import br.com.apsoo.dao.CaixaDAO;
import br.com.apsoo.dao.VendaDAO;
import br.com.apsoo.model.Caixa;
import br.com.apsoo.model.CpfCnpj;
import br.com.apsoo.model.Dinheiro;
import br.com.apsoo.model.ItemVenda;
import br.com.apsoo.model.Pagamento;
import br.com.apsoo.model.Pix;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.Venda;

public class VendaController {

    private final VendaDAO vendaDAO = new VendaDAO();
    private final CaixaDAO caixaDAO = new CaixaDAO();
    private Venda venda;

    public boolean isVendaIniciada() {
        return venda != null;
    }

    // Começa uma venda nova, descartando a anterior
    public void iniciarVenda(String cpfCnpj) throws RegraNegocioException, SQLException {
        venda = null;
        identificarCliente(cpfCnpj);
    }

    // Passo 1 da venda. Se a venda já existe (o vendedor voltou ao primeiro passo), só troca o cliente e
    // mantém os itens. Regra de Negócio: nenhuma venda pode ser registrada sem um caixa aberto.
    public void identificarCliente(String cpfCnpj) throws RegraNegocioException, SQLException {
        if (cpfCnpj.isBlank()) {
            throw new RegraNegocioException("CPF/CNPJ não informado!");
        }
        if (!CpfCnpj.isValido(cpfCnpj)) {
            throw new RegraNegocioException("CPF/CNPJ inválido!");
        }

        Caixa caixa = caixaDAO.buscarAberto();
        if (caixa == null) {
            throw new RegraNegocioException("Não há caixa aberto. Solicite ao administrador a abertura do caixa.");
        }

        if (venda == null) {
            venda = new Venda(Sessao.getUsuarioLogado(), caixa, CpfCnpj.limpar(cpfCnpj));
        } else {
            venda.setCpfCnpjCliente(CpfCnpj.limpar(cpfCnpj));
            venda.setCaixa(caixa);
        }
    }

    public void adicionarItem(Produto produto, int quantidade) throws RegraNegocioException {
        if (produto == null) {
            throw new RegraNegocioException("Produto não encontrado!");
        }
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade inválida!");
        }

        // Considera o que já foi adicionado na venda ao conferir o estoque
        int disponivel = produto.getQuantidadeAtual() - venda.quantidadeDoProduto(produto);
        if (quantidade > disponivel) {
            throw new RegraNegocioException("Quantidade superior à disponível. Disponível: " + disponivel);
        }

        venda.adicionarItem(produto, quantidade);
    }

    public void removerItem(ItemVenda item) {
        venda.removerItem(item);
    }

    public Venda getVenda() {
        return venda;
    }

    public BigDecimal calcularTotal() {
        return venda == null ? BigDecimal.ZERO : venda.calcularTotal();
    }

    public int registrarVenda(Pagamento pagamento) throws RegraNegocioException, SQLException {
        if (pagamento == null) {
            throw new RegraNegocioException("Método de pagamento não preenchido");
        }
        if (venda == null || venda.getItens().isEmpty()) {
            throw new RegraNegocioException("Adicione ao menos um produto à venda.");
        }
        if (pagamento instanceof Dinheiro dinheiro && !dinheiro.isValorSuficiente()) {
            throw new RegraNegocioException("A quantia informada não é suficiente.");
        }
        if (pagamento instanceof Pix pix && (pix.getComprovante() == null || pix.getComprovante().isBlank())) {
            throw new RegraNegocioException("Informe o código do pagamento Pix.");
        }

        venda.setPagamento(pagamento);
        int numero = vendaDAO.incluir(venda);
        venda = null;
        return numero;
    }

    public void cancelarVenda() {
        venda = null;
    }
}
