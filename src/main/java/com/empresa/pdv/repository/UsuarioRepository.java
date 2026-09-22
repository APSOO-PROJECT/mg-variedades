package com.empresa.pdv.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.empresa.pdv.models.Usuario;

public class UsuarioRepository {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:postgresql://db.lwnhinhawnvghnhrjoap.supabase.co:5432/postgres", 
            "postgres", 
            "20251907Apsoo@"
        );
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, login, senha_hash, cargo, status, data_cadastro FROM usuarios ORDER BY id ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("login"),
                    rs.getString("senha_hash"),
                    rs.getString("cargo"),
                    rs.getString("status")
                );
                if (rs.getTimestamp("data_cadastro") != null) {
                    u.setDataCadastro(rs.getTimestamp("data_cadastro").toLocalDateTime());
                }
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, login, senha_hash, cargo, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getSenhaHash());
            stmt.setString(4, usuario.getCargo());
            stmt.setString(5, usuario.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, cargo = ?, status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCargo());
            stmt.setString(3, usuario.getStatus());
            stmt.setLong(4, usuario.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Usuario> buscarPorLogin(String login) {
        String sql = "SELECT id, nome, login, senha_hash, cargo, status FROM usuarios WHERE login = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("login"),
                    rs.getString("senha_hash"),
                    rs.getString("cargo"),
                    rs.getString("status")
                );
                return Optional.of(usuario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}