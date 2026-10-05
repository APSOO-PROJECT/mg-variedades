package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import br.com.apsoo.model.MovimentacaoCaixa;

public class MovimentacaoCaixaDAO {

    // Movimentações são imutáveis: só há inclusão, nunca alteração ou exclusão (RF13, regra 3)
    public int incluir(MovimentacaoCaixa movimentacao) throws SQLException {
        String sql = "INSERT INTO movimentacao_caixa (caixa_id, tipo_operacao, valor, motivo, data_hora, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movimentacao.getCaixa().getId());
            stmt.setString(2, movimentacao.getTipoOperacao().name());
            stmt.setBigDecimal(3, movimentacao.getValor());
            stmt.setString(4, movimentacao.getMotivo());
            stmt.setTimestamp(5, Timestamp.valueOf(movimentacao.getDataHora()));
            stmt.setInt(6, movimentacao.getUsuario().getId());
            return stmt.executeUpdate();
        }
    }
}
