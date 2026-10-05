package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.apsoo.model.Categoria;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.StatusCadastro;
import br.com.apsoo.model.StatusProduto;

public class ProdutoDAO {

    private static final String SELECT = "SELECT p.id, p.nome, p.codigo_barras, p.custo_aquisicao, p.preco, "
            + "p.quantidade_atual, p.quantidade_minima, p.status, p.data_adicao, p.data_atualizacao, "
            + "p.usuario_atualizacao_id, u.nome AS usuario_nome, "
            + "c.id AS categoria_id, c.nome AS categoria_nome, "
            + "c.prazo_devolucao_horas, c.status AS categoria_status "
            + "FROM produto p JOIN categoria c ON c.id = p.categoria_id "
            + "LEFT JOIN usuario u ON u.id = p.usuario_atualizacao_id ";

    public int incluir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (nome, codigo_barras, custo_aquisicao, preco, quantidade_atual, "
                + "quantidade_minima, status, categoria_id, usuario_atualizacao_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherDados(stmt, produto);
            return stmt.executeUpdate();
        }
    }

    public int alterar(Produto produto) throws SQLException {
        if (produto.getId() == null) {
            throw new IllegalArgumentException("O id do produto é obrigatório para alteração.");
        }

        String sql = "UPDATE produto SET nome = ?, codigo_barras = ?, custo_aquisicao = ?, preco = ?, "
                + "quantidade_atual = ?, quantidade_minima = ?, status = ?, categoria_id = ?, "
                + "usuario_atualizacao_id = ?, data_atualizacao = NOW() WHERE id = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherDados(stmt, produto);
            stmt.setInt(10, produto.getId());
            return stmt.executeUpdate();
        }
    }

    public int excluir(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    // Produtos que atingiram a quantidade mínima (mesmo critério de Produto.sinalizarEstoqueBaixo)
    public int contarEstoqueBaixo() throws SQLException {
        String sql = "SELECT COUNT(*) FROM produto WHERE quantidade_atual <= quantidade_minima";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public boolean possuiVendas(int id) throws SQLException {
        String sql = "SELECT 1 FROM item_venda WHERE produto_id = ? LIMIT 1";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Produto buscarPorId(int id) throws SQLException {
        List<Produto> produtos = consultar(SELECT + "WHERE p.id = ?", id);
        return produtos.isEmpty() ? null : produtos.get(0);
    }

    public Produto buscarPorCodigoBarras(String codigoBarras) throws SQLException {
        List<Produto> produtos = consultar(SELECT + "WHERE p.codigo_barras = ?", codigoBarras);
        return produtos.isEmpty() ? null : produtos.get(0);
    }

    public List<Produto> listarTodos() throws SQLException {
        return consultar(SELECT + "ORDER BY p.nome");
    }

    // Produtos no mínimo (ou abaixo) cuja última atualização de estoque ocorreu no período [inicio, fim)
    public List<Produto> listarEstoqueBaixo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return consultar(SELECT + "WHERE p.quantidade_atual <= p.quantidade_minima "
                + "AND p.data_atualizacao >= ? AND p.data_atualizacao < ? ORDER BY p.data_atualizacao DESC, p.nome",
                Timestamp.valueOf(inicio), Timestamp.valueOf(fim));
    }

    // Busca produtos disponíveis pelo código de barras exato ou por parte do nome
    public List<Produto> buscarPorNomeOuCodigo(String termo) throws SQLException {
        return consultar(SELECT + "WHERE p.status = 'DISPONIVEL' AND (p.codigo_barras = ? OR p.nome LIKE ?) "
                + "ORDER BY p.nome LIMIT 10", termo, "%" + termo + "%");
    }

    private List<Produto> consultar(String sql, Object... parametros) throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(montarProduto(rs));
                }
            }
        }
        return produtos;
    }

    private void preencherDados(PreparedStatement stmt, Produto produto) throws SQLException {
        stmt.setString(1, produto.getNome());
        if (produto.getCodigoBarras() == null) {
            stmt.setNull(2, Types.VARCHAR);
        } else {
            stmt.setString(2, produto.getCodigoBarras());
        }
        stmt.setBigDecimal(3, produto.getCustoAquisicao());
        stmt.setBigDecimal(4, produto.getPreco());
        stmt.setInt(5, produto.getQuantidadeAtual());
        stmt.setInt(6, produto.getQuantidadeMinima());
        stmt.setString(7, produto.getStatus().name());
        stmt.setInt(8, produto.getCategoria().getId());
        stmt.setObject(9, produto.getUsuarioAtualizacaoId());
    }

    private Produto montarProduto(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getInt("categoria_id"),
                rs.getString("categoria_nome"),
                rs.getInt("prazo_devolucao_horas"),
                StatusCadastro.valueOf(rs.getString("categoria_status")));

        Produto produto = new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("codigo_barras"),
                rs.getBigDecimal("custo_aquisicao"),
                rs.getBigDecimal("preco"),
                rs.getInt("quantidade_atual"),
                rs.getInt("quantidade_minima"),
                StatusProduto.valueOf(rs.getString("status")),
                categoria,
                rs.getTimestamp("data_adicao").toLocalDateTime(),
                rs.getTimestamp("data_atualizacao").toLocalDateTime(),
                (Integer) rs.getObject("usuario_atualizacao_id"));
        produto.setUsuarioAtualizacaoNome(rs.getString("usuario_nome"));
        return produto;
    }
}
