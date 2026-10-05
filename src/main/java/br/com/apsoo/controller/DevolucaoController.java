package br.com.apsoo.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.com.apsoo.dao.CaixaDAO;
import br.com.apsoo.dao.DevolucaoDAO;
import br.com.apsoo.dao.VendaDAO;
import br.com.apsoo.model.Caixa;
import br.com.apsoo.model.CondicaoProduto;
import br.com.apsoo.model.CpfCnpj;
import br.com.apsoo.model.Devolucao;
import br.com.apsoo.model.FormaPagamento;
import br.com.apsoo.model.ItemVenda;
import br.com.apsoo.model.Venda;

// Casos de uso Localizar Venda e Registrar Devolução
public class DevolucaoController {

    private final VendaDAO vendaDAO = new VendaDAO();
    private final DevolucaoDAO devolucaoDAO = new DevolucaoDAO();
    private final CaixaDAO caixaDAO = new CaixaDAO();

    public Venda localizarPorNumero(String numero) throws RegraNegocioException, SQLException {
        if (numero.isBlank()) {
            throw new RegraNegocioException("Número da venda não informado!");
        }
        int id;
        try {
            id = Integer.parseInt(numero.trim());
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Número da venda inválido!");
        }

        Venda venda = vendaDAO.buscarPorId(id);
        if (venda == null) {
            throw new RegraNegocioException("Venda não encontrada!");
        }
        return venda;
    }

    public List<Venda> localizarPorCliente(String cpfCnpj) throws RegraNegocioException, SQLException {
        if (cpfCnpj.isBlank()) {
            throw new RegraNegocioException("CPF/CNPJ não informado!");
        }
        if (!CpfCnpj.isValido(cpfCnpj)) {
            throw new RegraNegocioException("CPF/CNPJ inválido!");
        }

        List<Venda> vendas = vendaDAO.buscarPorCpfCnpj(CpfCnpj.limpar(cpfCnpj));
        if (vendas.isEmpty()) {
            throw new RegraNegocioException("Nenhuma venda encontrada para este cliente.");
        }
        return vendas;
    }

    // Recarrega a venda para refletir as quantidades já devolvidas
    public Venda recarregar(Venda venda) throws SQLException {
        return vendaDAO.buscarPorId(venda.getId());
    }

    public int registrarDevolucao(Venda venda, Map<ItemVenda, Integer> quantidades, CondicaoProduto condicao,
                                  FormaPagamento reembolso, String motivo) throws RegraNegocioException, SQLException {
        if (venda == null) {
            throw new RegraNegocioException("Localize a venda a ser devolvida.");
        }

        Devolucao devolucao = new Devolucao(venda, Sessao.getUsuarioLogado(), condicao, motivo.trim(), reembolso);
        LocalDateTime agora = LocalDateTime.now();

        for (Map.Entry<ItemVenda, Integer> entrada : quantidades.entrySet()) {
            ItemVenda item = entrada.getKey();
            int quantidade = entrada.getValue();
            if (quantidade == 0) {
                continue;
            }
            if (quantidade < 0 || quantidade > item.quantidadeDevolvivel()) {
                throw new RegraNegocioException("Quantidade inválida para " + item.getProduto().getNome() + "!");
            }
            // Regra de Negócio: devolução fora do prazo da categoria é negada
            if (!venda.isDentroDoPrazo(item, agora)) {
                throw new RegraNegocioException("Prazo de devolução expirado para " + item.getProduto().getNome()
                        + " (prazo da categoria " + item.getProduto().getCategoria().getNome() + ": "
                        + item.getProduto().getCategoria().descricaoPrazo() + ").");
            }
            devolucao.adicionarItem(item, quantidade);
        }

        if (devolucao.getItens().isEmpty()) {
            throw new RegraNegocioException("Selecione ao menos um item para devolver.");
        }
        if (condicao == null) {
            throw new RegraNegocioException("Informe a condição do produto.");
        }
        if (reembolso == null) {
            throw new RegraNegocioException("Informe o tipo de reembolso.");
        }
        if (motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo da devolução.");
        }

        // Regra de Negócio: reembolso em dinheiro exige caixa aberto e gera saída no caixa
        Integer caixaId = null;
        if (reembolso == FormaPagamento.DINHEIRO) {
            Caixa caixa = caixaDAO.buscarAberto();
            if (caixa == null) {
                throw new RegraNegocioException("Não é possível reembolsar em dinheiro sem um caixa aberto.");
            }
            caixaId = caixa.getId();
        }

        return devolucaoDAO.incluir(devolucao, caixaId);
    }
}
