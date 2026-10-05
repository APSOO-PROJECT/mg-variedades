package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.apsoo.model.Cartao;
import br.com.apsoo.model.Categoria;
import br.com.apsoo.model.Dinheiro;
import br.com.apsoo.model.ItemVenda;
import br.com.apsoo.model.ModalidadeCartao;
import br.com.apsoo.model.Pagamento;
import br.com.apsoo.model.Pix;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.StatusCadastro;
import br.com.apsoo.model.Usuario;
import br.com.apsoo.model.Venda;

public class VendaDAO {

    private static final String SELECT_VENDA = "SELECT v.id, v.valor_total, v.data_hora, v.cpf_cnpj_cliente, "
            + "u.id AS usuario_id, u.nome AS usuario_nome, "
            + "p.id AS pagamento_id, p.forma_pagamento, p.valor_venda, p.valor_recebido, "
            + "px.comprovante, ca.modalidade, ca.taxa, ca.parcelas "
            + "FROM venda v "
            + "JOIN usuario u ON u.id = v.usuario_id "
            + "JOIN pagamento p ON p.id = v.pagamento_id "
            + "LEFT JOIN pix px ON px.id = p.id "
            + "LEFT JOIN cartao ca ON ca.id = p.id ";

    public Venda buscarPorId(int id) throws SQLException {
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SELECT_VENDA + "WHERE v.id = ?")) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Venda venda = montarVenda(rs);
                    carregarItens(conn, venda);
                    return venda;
                }
            }
        }
        return null;
    }

    // Vendas mais recentes primeiro
    public List<Venda> buscarPorCpfCnpj(String cpfCnpj) throws SQLException {
        List<Venda> vendas = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_VENDA + "WHERE v.cpf_cnpj_cliente = ? ORDER BY v.data_hora DESC")) {
            stmt.setString(1, cpfCnpj);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vendas.add(montarVenda(rs));
                }
            }
            for (Venda venda : vendas) {
                carregarItens(conn, venda);
            }
        }
        return vendas;
    }

    private Venda montarVenda(ResultSet rs) throws SQLException {
        Venda venda = new Venda();
        venda.setId(rs.getInt("id"));
        venda.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        venda.setCpfCnpjCliente(rs.getString("cpf_cnpj_cliente"));

        Usuario vendedor = new Usuario();
        vendedor.setId(rs.getInt("usuario_id"));
        vendedor.setNome(rs.getString("usuario_nome"));
        venda.setUsuario(vendedor);

        var valorVenda = rs.getBigDecimal("valor_venda");
        var valorRecebido = rs.getBigDecimal("valor_recebido");
        Pagamento pagamento = switch (rs.getString("forma_pagamento")) {
            case "DINHEIRO" -> new Dinheiro(valorVenda, valorRecebido);
            case "PIX" -> new Pix(valorVenda, rs.getString("comprovante"));
            default -> {
                Cartao cartao = new Cartao(valorVenda, ModalidadeCartao.valueOf(rs.getString("modalidade")),
                        rs.getInt("parcelas"));
                cartao.setTaxa(rs.getBigDecimal("taxa"));
                yield cartao;
            }
        };
        pagamento.setId(rs.getInt("pagamento_id"));
        venda.setPagamento(pagamento);
        return venda;
    }

    private void carregarItens(Connection conn, Venda venda) throws SQLException {
        String sql = "SELECT iv.id, iv.quantidade, iv.preco_unitario, iv.custo_unitario, "
                + "pr.id AS produto_id, pr.nome AS produto_nome, "
                + "c.id AS categoria_id, c.nome AS categoria_nome, c.prazo_devolucao_horas, c.status AS categoria_status, "
                + "COALESCE((SELECT SUM(idv.quantidade) FROM item_devolucao idv WHERE idv.item_venda_id = iv.id), 0) AS devolvida "
                + "FROM item_venda iv "
                + "JOIN produto pr ON pr.id = iv.produto_id "
                + "JOIN categoria c ON c.id = pr.categoria_id "
                + "WHERE iv.venda_id = ? ORDER BY iv.id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venda.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Categoria categoria = new Categoria(
                            rs.getInt("categoria_id"),
                            rs.getString("categoria_nome"),
                            rs.getInt("prazo_devolucao_horas"),
                            StatusCadastro.valueOf(rs.getString("categoria_status")));

                    Produto produto = new Produto();
                    produto.setId(rs.getInt("produto_id"));
                    produto.setNome(rs.getString("produto_nome"));
                    produto.setCategoria(categoria);

                    ItemVenda item = new ItemVenda();
                    item.setId(rs.getInt("id"));
                    item.setProduto(produto);
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                    item.setCustoUnitario(rs.getBigDecimal("custo_unitario"));
                    item.setQuantidadeDevolvida(rs.getInt("devolvida"));
                    venda.getItens().add(item);
                }
            }
        }
    }

    // Grava pagamento, venda, itens e baixa de estoque na mesma transação
    public int incluir(Venda venda) throws SQLException {
        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false);
            try {
                venda.setDataHora(LocalDateTime.now());
                incluirPagamento(conn, venda.getPagamento(), venda.getCaixa().getId());
                incluirVenda(conn, venda);
                for (ItemVenda item : venda.getItens()) {
                    incluirItem(conn, venda, item);
                    baixarEstoque(conn, venda, item);
                }
                conn.commit();
                return venda.getId();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void incluirPagamento(Connection conn, Pagamento pagamento, int caixaId) throws SQLException {
        String sql = "INSERT INTO pagamento (forma_pagamento, valor_venda, valor_recebido, caixa_id) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pagamento.getFormaPagamento().name());
            stmt.setBigDecimal(2, pagamento.getValorVenda());
            stmt.setBigDecimal(3, pagamento.getValorRecebido());
            stmt.setInt(4, caixaId);
            stmt.executeUpdate();
            pagamento.setId(lerChaveGerada(stmt));
        }

        if (pagamento instanceof Dinheiro dinheiro) {
            executar(conn, "INSERT INTO dinheiro (id, valor_recebido, troco) VALUES (?, ?, ?)",
                    pagamento.getId(), dinheiro.getValorRecebido(), dinheiro.calcularTroco());
        } else if (pagamento instanceof Pix pix) {
            executar(conn, "INSERT INTO pix (id, comprovante) VALUES (?, ?)",
                    pagamento.getId(), pix.getComprovante());
        } else if (pagamento instanceof Cartao cartao) {
            executar(conn, "INSERT INTO cartao (id, modalidade, taxa, parcelas) VALUES (?, ?, ?, ?)",
                    pagamento.getId(), cartao.getModalidade().name(), cartao.getTaxa(), cartao.getParcelas());
        }
    }

    private void incluirVenda(Connection conn, Venda venda) throws SQLException {
        String sql = "INSERT INTO venda (valor_total, data_hora, cpf_cnpj_cliente, pagamento_id, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBigDecimal(1, venda.calcularTotal());
            stmt.setTimestamp(2, Timestamp.valueOf(venda.getDataHora()));
            stmt.setString(3, venda.getCpfCnpjCliente());
            stmt.setInt(4, venda.getPagamento().getId());
            stmt.setInt(5, venda.getUsuario().getId());
            stmt.executeUpdate();
            venda.setId(lerChaveGerada(stmt));
        }
    }

    private void incluirItem(Connection conn, Venda venda, ItemVenda item) throws SQLException {
        executar(conn,
                "INSERT INTO item_venda (venda_id, produto_id, quantidade, preco_unitario, custo_unitario, subtotal) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                venda.getId(), item.getProduto().getId(), item.getQuantidade(),
                item.getPrecoUnitario(), item.getCustoUnitario(), item.calcularSubtotal());
    }

    // A condição quantidade_atual >= ? impede estoque negativo em vendas simultâneas
    private void baixarEstoque(Connection conn, Venda venda, ItemVenda item) throws SQLException {
        String sql = "UPDATE produto SET quantidade_atual = quantidade_atual - ?, "
                + "data_atualizacao = NOW(), usuario_atualizacao_id = ? "
                + "WHERE id = ? AND quantidade_atual >= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getQuantidade());
            stmt.setInt(2, venda.getUsuario().getId());
            stmt.setInt(3, item.getProduto().getId());
            stmt.setInt(4, item.getQuantidade());
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Estoque insuficiente para o produto: " + item.getProduto().getNome());
            }
        }
    }

    private void executar(Connection conn, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            stmt.executeUpdate();
        }
    }

    private int lerChaveGerada(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
