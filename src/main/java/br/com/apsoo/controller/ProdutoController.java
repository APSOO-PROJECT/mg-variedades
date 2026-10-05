package br.com.apsoo.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.apsoo.dao.CategoriaDAO;
import br.com.apsoo.dao.ProdutoDAO;
import br.com.apsoo.model.Categoria;
import br.com.apsoo.model.Produto;

public class ProdutoController {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // Usado no autocompletar: aceita parte do nome ou o código de barras
    public List<Produto> buscar(String termo) throws SQLException {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }
        return produtoDAO.buscarPorNomeOuCodigo(termo.trim());
    }

    // Consulta de estoque (RF14): disponível para qualquer usuário autenticado
    public List<Produto> consultarEstoque() throws SQLException {
        return produtoDAO.listarTodos();
    }

    public List<Produto> listar() throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        return produtoDAO.listarTodos();
    }

    // Categoria desativada não aparece para novos produtos, mas o produto já cadastrado nela a mantém (RF05, regra 4)
    public List<Categoria> categoriasDisponiveis(Produto produto) throws SQLException {
        List<Categoria> categorias = new ArrayList<>(categoriaDAO.listarAtivas());
        if (produto != null && produto.getCategoria() != null
                && categorias.stream().noneMatch(c -> c.getId().equals(produto.getCategoria().getId()))) {
            categorias.add(produto.getCategoria());
        }
        return categorias;
    }

    public void salvar(Produto produto) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();

        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new RegraNegocioException("Informe o nome do produto.");
        }
        if (produto.getNome().length() > 60) {
            throw new RegraNegocioException("O nome do produto deve ter no máximo 60 caracteres.");
        }
        if (produto.getCategoria() == null) {
            throw new RegraNegocioException("Selecione a categoria do produto.");
        }
        if (produto.getCustoAquisicao() == null || produto.getCustoAquisicao().signum() < 0) {
            throw new RegraNegocioException("Informe um custo de aquisição válido.");
        }
        if (produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Informe um preço de venda maior que zero.");
        }
        if (produto.getQuantidadeAtual() < 0 || produto.getQuantidadeMinima() < 0) {
            throw new RegraNegocioException("As quantidades não podem ser negativas.");
        }

        if (produto.getCodigoBarras() != null) {
            Produto existente = produtoDAO.buscarPorCodigoBarras(produto.getCodigoBarras());
            if (existente != null && !existente.getId().equals(produto.getId())) {
                throw new RegraNegocioException("Já existe um produto com este código de barras.");
            }
        }

        produto.setUsuarioAtualizacaoId(Sessao.getUsuarioLogado().getId());
        if (produto.getId() == null) {
            if (!produto.getCategoria().isAtiva()) {
                throw new RegraNegocioException("Categoria desativada não pode ser usada em novos produtos.");
            }
            produtoDAO.incluir(produto);
        } else {
            Produto atual = produtoDAO.buscarPorId(produto.getId());
            boolean mudouCategoria = !atual.getCategoria().getId().equals(produto.getCategoria().getId());
            if (mudouCategoria && !produto.getCategoria().isAtiva()) {
                throw new RegraNegocioException("Categoria desativada não pode ser selecionada.");
            }
            produtoDAO.alterar(produto);
        }
    }

    // Produto com vendas registradas não pode ser removido; deve ficar indisponível
    public void remover(Produto produto) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        if (produtoDAO.possuiVendas(produto.getId())) {
            throw new RegraNegocioException("Este produto possui vendas registradas e não pode ser removido. "
                    + "Altere o status para indisponível.");
        }
        produtoDAO.excluir(produto.getId());
    }
}
