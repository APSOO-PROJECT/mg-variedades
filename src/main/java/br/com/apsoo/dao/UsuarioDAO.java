package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.apsoo.model.Cargo;
import br.com.apsoo.model.StatusCadastro;
import br.com.apsoo.model.Usuario;

public class UsuarioDAO {

    private static final String SELECT = "SELECT id, nome, login, senha, cargo, status, seed, data_cadastro "
            + "FROM usuario ";

    public int incluir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, login, senha, cargo, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getCargo().name());
            stmt.setString(5, usuario.getStatus().name());
            return stmt.executeUpdate();
        }
    }

    // A senha só é gravada quando atualizarSenha for true
    public int alterar(Usuario usuario, boolean atualizarSenha) throws SQLException {
        if (usuario.getId() == null) {
            throw new IllegalArgumentException("O id do usuário é obrigatório para alteração.");
        }

        String sql = atualizarSenha
                ? "UPDATE usuario SET nome = ?, login = ?, cargo = ?, status = ?, senha = ? WHERE id = ?"
                : "UPDATE usuario SET nome = ?, login = ?, cargo = ?, status = ? WHERE id = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getCargo().name());
            stmt.setString(4, usuario.getStatus().name());
            if (atualizarSenha) {
                stmt.setString(5, usuario.getSenha());
                stmt.setInt(6, usuario.getId());
            } else {
                stmt.setInt(5, usuario.getId());
            }
            return stmt.executeUpdate();
        }
    }

    public int alterarSenha(int id, String senhaHash) throws SQLException {
        String sql = "UPDATE usuario SET senha = ? WHERE id = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, senhaHash);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {
        List<Usuario> usuarios = consultar(SELECT + "WHERE id = ?", id);
        return usuarios.isEmpty() ? null : usuarios.get(0);
    }

    public Usuario buscarPorLogin(String login) throws SQLException {
        List<Usuario> usuarios = consultar(SELECT + "WHERE login = ?", login);
        return usuarios.isEmpty() ? null : usuarios.get(0);
    }

    public List<Usuario> listarTodos() throws SQLException {
        return consultar(SELECT + "ORDER BY nome");
    }

    private List<Usuario> consultar(String sql, Object... parametros) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(new Usuario(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("login"),
                            rs.getString("senha"),
                            Cargo.valueOf(rs.getString("cargo")),
                            StatusCadastro.valueOf(rs.getString("status")),
                            rs.getBoolean("seed"),
                            rs.getTimestamp("data_cadastro").toLocalDateTime()));
                }
            }
        }
        return usuarios;
    }
}
