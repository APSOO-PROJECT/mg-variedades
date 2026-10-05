package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.apsoo.model.Categoria;
import br.com.apsoo.model.CategoriaHistorico;
import br.com.apsoo.model.StatusCadastro;

public class CategoriaDAO {

    private static final String SELECT = "SELECT c.id, c.nome, c.prazo_devolucao_horas, c.status, "
            + "(SELECT COUNT(*) FROM produto p WHERE p.categoria_id = c.id) AS qtd_produtos "
            + "FROM categoria c ";

    public int incluir(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nome, prazo_devolucao_horas, status) VALUES (?, ?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setInt(2, categoria.getPrazoDevolucaoHoras());
            stmt.setString(3, categoria.getStatus().name());
            return stmt.executeUpdate();
        }
    }

    // Atualiza a categoria e registra no histórico cada campo que mudou (RF05, regra 6)
    public int alterar(Categoria categoria, int usuarioId) throws SQLException {
        if (categoria.getId() == null) {
            throw new IllegalArgumentException("O id da categoria é obrigatório para alteração.");
        }

        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                Categoria anterior = buscarPorId(conn, categoria.getId());
                int linhas;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE categoria SET nome = ?, prazo_devolucao_horas = ?, status = ? WHERE id = ?")) {
                    stmt.setString(1, categoria.getNome());
                    stmt.setInt(2, categoria.getPrazoDevolucaoHoras());
                    stmt.setString(3, categoria.getStatus().name());
                    stmt.setInt(4, categoria.getId());
                    linhas = stmt.executeUpdate();
                }

                registrarMudanca(conn, categoria, usuarioId, "Nome", anterior.getNome(), categoria.getNome());
                registrarMudanca(conn, categoria, usuarioId, "Prazo de devolução",
                        anterior.descricaoPrazo(), categoria.descricaoPrazo());
                registrarMudanca(conn, categoria, usuarioId, "Status",
                        anterior.getStatus().name(), categoria.getStatus().name());
                conn.commit();
                return linhas;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int excluir(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    public Categoria buscarPorId(int id) throws SQLException {
        try (Connection conn = Conexao.getConexao()) {
            return buscarPorId(conn, id);
        }
    }

    public Categoria buscarPorNome(String nome) throws SQLException {
        List<Categoria> categorias = consultar(SELECT + "WHERE c.nome = ?", nome);
        return categorias.isEmpty() ? null : categorias.get(0);
    }

    public List<Categoria> listarTodas() throws SQLException {
        return consultar(SELECT + "ORDER BY c.nome");
    }

    public List<Categoria> listarAtivas() throws SQLException {
        return consultar(SELECT + "WHERE c.status = 'ATIVO' ORDER BY c.nome");
    }

    public List<CategoriaHistorico> listarHistorico(int categoriaId) throws SQLException {
        String sql = "SELECT h.data_alteracao, h.campo, h.valor_anterior, h.valor_novo, u.nome "
                + "FROM categoria_historico h JOIN usuario u ON u.id = h.usuario_id "
                + "WHERE h.categoria_id = ? ORDER BY h.data_alteracao DESC, h.id DESC";

        List<CategoriaHistorico> historico = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historico.add(new CategoriaHistorico(
                            rs.getTimestamp(1).toLocalDateTime(),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5)));
                }
            }
        }
        return historico;
    }

    private Categoria buscarPorId(Connection conn, int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT + "WHERE c.id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? montarCategoria(rs) : null;
            }
        }
    }

    private void registrarMudanca(Connection conn, Categoria categoria, int usuarioId, String campo,
                                  String anterior, String novo) throws SQLException {
        if (anterior.equals(novo)) {
            return;
        }
        String sql = "INSERT INTO categoria_historico "
                + "(categoria_id, data_alteracao, campo, valor_anterior, valor_novo, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoria.getId());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, campo);
            stmt.setString(4, anterior);
            stmt.setString(5, novo);
            stmt.setInt(6, usuarioId);
            stmt.executeUpdate();
        }
    }

    private List<Categoria> consultar(String sql, Object... parametros) throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categorias.add(montarCategoria(rs));
                }
            }
        }
        return categorias;
    }

    private Categoria montarCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getInt("prazo_devolucao_horas"),
                StatusCadastro.valueOf(rs.getString("status")));
        categoria.setQuantidadeProdutos(rs.getInt("qtd_produtos"));
        return categoria;
    }
}
