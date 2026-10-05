package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import br.com.apsoo.model.Devolucao;
import br.com.apsoo.model.FormaPagamento;
import br.com.apsoo.model.ItemDevolucao;

public class DevolucaoDAO {

    // Grava a devolução e, se o reembolso for em dinheiro, a saída no caixa aberto (caixaId)
    public int incluir(Devolucao devolucao, Integer caixaId) throws SQLException {
        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                devolucao.setDataHora(LocalDateTime.now());
                incluirDevolucao(conn, devolucao);
                for (ItemDevolucao item : devolucao.getItens()) {
                    incluirItem(conn, devolucao, item);
                }
                if (devolucao.getTipoReembolso() == FormaPagamento.DINHEIRO) {
                    incluirSaidaCaixa(conn, devolucao, caixaId);
                }
                conn.commit();
                return devolucao.getId();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void incluirDevolucao(Connection conn, Devolucao devolucao) throws SQLException {
        String sql = "INSERT INTO devolucao (venda_id, data_hora, status_produto, motivo, "
                + "tipo_reembolso, valor_reembolsado, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, devolucao.getVenda().getId());
            stmt.setTimestamp(2, Timestamp.valueOf(devolucao.getDataHora()));
            stmt.setString(3, devolucao.getCondicaoProduto().name());
            stmt.setString(4, devolucao.getMotivo());
            stmt.setString(5, devolucao.getTipoReembolso().name());
            stmt.setBigDecimal(6, devolucao.calcularValorReembolsado());
            stmt.setInt(7, devolucao.getUsuario().getId());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                devolucao.setId(rs.getInt(1));
            }
        }
    }

    // Confere dentro da transação que o item não foi devolvido além do que foi vendido
    private void incluirItem(Connection conn, Devolucao devolucao, ItemDevolucao item) throws SQLException {
        int itemVendaId = item.getItemVenda().getId();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COALESCE(SUM(quantidade), 0) FROM item_devolucao WHERE item_venda_id = ? FOR UPDATE")) {
            stmt.setInt(1, itemVendaId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) + item.getQuantidade() > item.getItemVenda().getQuantidade()) {
                    throw new SQLException("Quantidade a devolver excede a quantidade vendida de: "
                            + item.getItemVenda().getProduto().getNome());
                }
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO item_devolucao (devolucao_id, item_venda_id, quantidade) VALUES (?, ?, ?)")) {
            stmt.setInt(1, devolucao.getId());
            stmt.setInt(2, itemVendaId);
            stmt.setInt(3, item.getQuantidade());
            stmt.executeUpdate();
        }
    }

    private void incluirSaidaCaixa(Connection conn, Devolucao devolucao, int caixaId) throws SQLException {
        String sql = "INSERT INTO movimentacao_caixa (caixa_id, tipo_operacao, valor, motivo, data_hora, usuario_id) "
                + "VALUES (?, 'REEMBOLSO', ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caixaId);
            stmt.setBigDecimal(2, devolucao.calcularValorReembolsado());
            stmt.setString(3, "Reembolso da devolução da venda nº " + devolucao.getVenda().getId());
            stmt.setTimestamp(4, Timestamp.valueOf(devolucao.getDataHora()));
            stmt.setInt(5, devolucao.getUsuario().getId());
            stmt.executeUpdate();
        }
    }
}
