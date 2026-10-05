package br.com.apsoo.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.com.apsoo.model.Caixa;
import br.com.apsoo.model.EventoCaixa;
import br.com.apsoo.model.ResumoCaixa;

public class CaixaDAO {

    // Retorna o caixa em aberto ou null quando não há nenhum
    public Caixa buscarAberto() throws SQLException {
        String sql = "SELECT c.id, c.fundo_troco_inicial, c.data_hora_abertura, u.nome "
                + "FROM caixa c JOIN usuario u ON u.id = c.usuario_abertura_id "
                + "WHERE c.status = 'ABERTO' LIMIT 1";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Caixa caixa = new Caixa(
                        rs.getInt("id"),
                        rs.getBigDecimal("fundo_troco_inicial"),
                        rs.getTimestamp("data_hora_abertura").toLocalDateTime());
                caixa.setUsuarioAberturaNome(rs.getString("nome"));
                return caixa;
            }
        }
        return null;
    }

    public int incluir(Caixa caixa, int usuarioId) throws SQLException {
        String sql = "INSERT INTO caixa (fundo_troco_inicial, data_hora_abertura, status, usuario_abertura_id) "
                + "VALUES (?, ?, 'ABERTO', ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, caixa.getFundoTrocoInicial());
            stmt.setTimestamp(2, Timestamp.valueOf(caixa.getDataHoraAbertura()));
            stmt.setInt(3, usuarioId);
            return stmt.executeUpdate();
        }
    }

    // Só fecha um caixa que ainda esteja aberto
    public int fechar(Caixa caixa, int usuarioId) throws SQLException {
        String sql = "UPDATE caixa SET status = 'FECHADO', data_hora_fechamento = ?, saldo_esperado = ?, "
                + "saldo_real = ?, justificativa_quebra = ?, usuario_fechamento_id = ? "
                + "WHERE id = ? AND status = 'ABERTO'";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(caixa.getDataHoraFechamento()));
            stmt.setBigDecimal(2, caixa.getSaldoEsperado());
            stmt.setBigDecimal(3, caixa.getSaldoReal());
            stmt.setString(4, caixa.getJustificativaQuebra());
            stmt.setInt(5, usuarioId);
            stmt.setInt(6, caixa.getId());
            return stmt.executeUpdate();
        }
    }

    // Totais do caixa; o lucro considera o custo de aquisição e desconta itens devolvidos (RF10)
    public ResumoCaixa resumir(Caixa caixa) throws SQLException {
        ResumoCaixa resumo = new ResumoCaixa();
        resumo.setFundoTrocoInicial(caixa.getFundoTrocoInicial());

        try (Connection conn = Conexao.getConexao()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT forma_pagamento, SUM(valor_venda) FROM pagamento WHERE caixa_id = ? GROUP BY forma_pagamento")) {
                stmt.setInt(1, caixa.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        switch (rs.getString(1)) {
                            case "DINHEIRO" -> resumo.setVendasDinheiro(rs.getBigDecimal(2));
                            case "PIX" -> resumo.setVendasPix(rs.getBigDecimal(2));
                            default -> resumo.setVendasCartao(rs.getBigDecimal(2));
                        }
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT tipo_operacao, SUM(valor) FROM movimentacao_caixa WHERE caixa_id = ? GROUP BY tipo_operacao")) {
                stmt.setInt(1, caixa.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        switch (rs.getString(1)) {
                            case "SUPRIMENTO" -> resumo.setSuprimentos(rs.getBigDecimal(2));
                            case "SANGRIA" -> resumo.setSangrias(rs.getBigDecimal(2));
                            default -> resumo.setReembolsosDinheiro(rs.getBigDecimal(2));
                        }
                    }
                }
            }

            String sqlLucro = "SELECT COALESCE(SUM((iv.preco_unitario - iv.custo_unitario) "
                    + "* (iv.quantidade - COALESCE(d.devolvida, 0))), 0) "
                    + "FROM item_venda iv "
                    + "JOIN venda v ON v.id = iv.venda_id "
                    + "JOIN pagamento p ON p.id = v.pagamento_id "
                    + "LEFT JOIN (SELECT item_venda_id, SUM(quantidade) AS devolvida "
                    + "           FROM item_devolucao GROUP BY item_venda_id) d ON d.item_venda_id = iv.id "
                    + "WHERE p.caixa_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLucro)) {
                stmt.setInt(1, caixa.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    resumo.setLucro(rs.getBigDecimal(1));
                }
            }
        }
        return resumo;
    }

    // Histórico do período [inicio, fim): aberturas, fechamentos e movimentações, do mais recente ao mais antigo
    public List<EventoCaixa> listarEventos(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<EventoCaixa> eventos = new ArrayList<>();

        String sqlAberturas = "SELECT c.id, c.data_hora_abertura, c.fundo_troco_inicial, u.nome "
                + "FROM caixa c JOIN usuario u ON u.id = c.usuario_abertura_id "
                + "WHERE c.data_hora_abertura >= ? AND c.data_hora_abertura < ?";
        String sqlFechamentos = "SELECT c.id, c.data_hora_fechamento, c.saldo_real, c.saldo_esperado, "
                + "c.justificativa_quebra, u.nome "
                + "FROM caixa c JOIN usuario u ON u.id = c.usuario_fechamento_id "
                + "WHERE c.data_hora_fechamento >= ? AND c.data_hora_fechamento < ?";
        String sqlMovimentacoes = "SELECT m.caixa_id, m.data_hora, m.tipo_operacao, m.valor, m.motivo, u.nome "
                + "FROM movimentacao_caixa m JOIN usuario u ON u.id = m.usuario_id "
                + "WHERE m.data_hora >= ? AND m.data_hora < ?";

        try (Connection conn = Conexao.getConexao()) {
            try (ResultSet rs = consultarPeriodo(conn, sqlAberturas, inicio, fim)) {
                while (rs.next()) {
                    eventos.add(new EventoCaixa(rs.getTimestamp(2).toLocalDateTime(), "Abertura",
                            rs.getBigDecimal(3), "Fundo de troco inicial", rs.getString(4), rs.getInt(1)));
                }
            }
            try (ResultSet rs = consultarPeriodo(conn, sqlFechamentos, inicio, fim)) {
                while (rs.next()) {
                    EventoCaixa fechamento = new EventoCaixa(rs.getTimestamp(2).toLocalDateTime(), "Fechamento",
                            rs.getBigDecimal(3), rs.getString(5), rs.getString(6), rs.getInt(1));
                    fechamento.setValorEsperado(rs.getBigDecimal(4));
                    eventos.add(fechamento);
                }
            }
            try (ResultSet rs = consultarPeriodo(conn, sqlMovimentacoes, inicio, fim)) {
                while (rs.next()) {
                    eventos.add(new EventoCaixa(rs.getTimestamp(2).toLocalDateTime(), descricaoTipo(rs.getString(3)),
                            rs.getBigDecimal(4), rs.getString(5), rs.getString(6), rs.getInt(1)));
                }
            }
        }

        eventos.sort(Comparator.comparing(EventoCaixa::getDataHora).reversed());
        return eventos;
    }

    private ResultSet consultarPeriodo(Connection conn, String sql, LocalDateTime inicio, LocalDateTime fim)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.closeOnCompletion();
        stmt.setTimestamp(1, Timestamp.valueOf(inicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fim));
        return stmt.executeQuery();
    }

    private String descricaoTipo(String tipo) {
        return switch (tipo) {
            case "SANGRIA" -> "Sangria";
            case "SUPRIMENTO" -> "Suprimento";
            default -> "Reembolso";
        };
    }
}
