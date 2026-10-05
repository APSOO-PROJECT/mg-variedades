package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.apsoo.model.CondicaoProduto;
import br.com.apsoo.model.FormaPagamento;
import br.com.apsoo.model.LinhaVendaCategoria;
import br.com.apsoo.model.MetricasDia;
import br.com.apsoo.model.ProdutoVendido;
import br.com.apsoo.model.RelatorioDevolucoes;
import br.com.apsoo.model.RelatorioProdutosVendidos;
import br.com.apsoo.model.RelatorioVendas;
import br.com.apsoo.model.VendaProduto;
import br.com.apsoo.model.VendasVendedor;

// Consultas agregadas de vendas usadas nos relatórios e no painel inicial
public class RelatorioDAO {

    // Vendas do período [inicio, fim)
    public RelatorioVendas consultarVendas(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        RelatorioVendas relatorio = new RelatorioVendas();

        try (Connection conn = Conexao.getConexao()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(valor_total), 0) FROM venda WHERE data_hora >= ? AND data_hora < ?")) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    relatorio.setQuantidadeVendas(rs.getInt(1));
                    relatorio.setValorTotal(rs.getBigDecimal(2));
                }
            }

            String sqlForma = "SELECT p.forma_pagamento, SUM(p.valor_venda) FROM venda v "
                    + "JOIN pagamento p ON p.id = v.pagamento_id "
                    + "WHERE v.data_hora >= ? AND v.data_hora < ? GROUP BY p.forma_pagamento";
            try (PreparedStatement stmt = conn.prepareStatement(sqlForma)) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        relatorio.getVendasPorForma().put(FormaPagamento.valueOf(rs.getString(1)), rs.getBigDecimal(2));
                    }
                }
            }

            relatorio.setProdutoMaisVendido(produtoMaisVendido(conn,
                    "v.data_hora >= ? AND v.data_hora < ?", Timestamp.valueOf(inicio), Timestamp.valueOf(fim)));
        }

        relatorio.setCategorias(listarVendasPorCategoria(inicio, fim));
        return relatorio;
    }

    // Todas as categorias cadastradas, inclusive as sem vendas (que aparecem zeradas)
    public List<LinhaVendaCategoria> listarVendasPorCategoria(LocalDateTime inicio, LocalDateTime fim)
            throws SQLException {
        Map<Integer, LinhaVendaCategoria> linhas = new LinkedHashMap<>();

        String sqlTotais = "SELECT c.id, c.nome, "
                + "COALESCE(SUM(CASE WHEN v.id IS NOT NULL THEN iv.quantidade ELSE 0 END), 0) AS qtd, "
                + "COALESCE(SUM(CASE WHEN v.id IS NOT NULL THEN iv.subtotal ELSE 0 END), 0) AS valor "
                + "FROM categoria c "
                + "LEFT JOIN produto p ON p.categoria_id = c.id "
                + "LEFT JOIN item_venda iv ON iv.produto_id = p.id "
                + "LEFT JOIN venda v ON v.id = iv.venda_id AND v.data_hora >= ? AND v.data_hora < ? "
                + "GROUP BY c.id, c.nome ORDER BY c.nome";

        // Ordenado do mais para o menos vendido; o primeiro é o mais vendido e o último, o menos vendido
        String sqlProdutos = "SELECT p.categoria_id, p.nome, SUM(iv.quantidade) AS qtd "
                + "FROM item_venda iv "
                + "JOIN venda v ON v.id = iv.venda_id "
                + "JOIN produto p ON p.id = iv.produto_id "
                + "WHERE v.data_hora >= ? AND v.data_hora < ? "
                + "GROUP BY p.categoria_id, p.id, p.nome ORDER BY qtd DESC, p.nome";

        String sqlAlteradas = "SELECT DISTINCT categoria_id FROM categoria_historico "
                + "WHERE data_alteracao >= ? AND data_alteracao < ?";

        try (Connection conn = Conexao.getConexao()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlTotais)) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        linhas.put(rs.getInt(1), new LinhaVendaCategoria(
                                rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4)));
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlProdutos)) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LinhaVendaCategoria linha = linhas.get(rs.getInt(1));
                        ProdutoVendido produto = new ProdutoVendido(rs.getString(2), rs.getInt(3));
                        if (linha.getMaisVendido() == null) {
                            linha.setMaisVendido(produto);
                        }
                        linha.setMenosVendido(produto);
                    }
                }
            }

            Set<Integer> alteradas = new HashSet<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlAlteradas)) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        alteradas.add(rs.getInt(1));
                    }
                }
            }
            for (LinhaVendaCategoria linha : linhas.values()) {
                linha.setAlteradaNoPeriodo(alteradas.contains(linha.getCategoriaId()));
            }
        }
        return new ArrayList<>(linhas.values());
    }

    // Métricas das vendas do caixa vigente; usuarioId limita às vendas de um vendedor (null = todas)
    public MetricasDia consultarMetricasDoCaixa(int caixaId, Integer usuarioId) throws SQLException {
        MetricasDia metricas = new MetricasDia();
        metricas.setCaixaAberto(true);

        String sql = "SELECT COUNT(*), COALESCE(SUM(v.valor_total), 0) FROM venda v "
                + "JOIN pagamento p ON p.id = v.pagamento_id WHERE p.caixa_id = ?"
                + (usuarioId == null ? "" : " AND v.usuario_id = ?");

        try (Connection conn = Conexao.getConexao()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, caixaId);
                if (usuarioId != null) {
                    stmt.setInt(2, usuarioId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    metricas.setQuantidadeVendas(rs.getInt(1));
                    metricas.setFaturamento(rs.getBigDecimal(2));
                }
            }
            metricas.setProdutoMaisVendido(produtoMaisVendido(conn,
                    "v.pagamento_id IN (SELECT id FROM pagamento WHERE caixa_id = ?)", caixaId));
        }
        return metricas;
    }

    // RF20: produto mais vendido, mais vendidos por categoria e produtos sem venda no período [inicio, fim)
    public RelatorioProdutosVendidos consultarProdutosVendidos(LocalDateTime inicio, LocalDateTime fim)
            throws SQLException {
        RelatorioProdutosVendidos relatorio = new RelatorioProdutosVendidos();

        String sqlVendidos = "SELECT c.nome, p.nome, SUM(iv.quantidade) AS qtd FROM item_venda iv "
                + "JOIN venda v ON v.id = iv.venda_id "
                + "JOIN produto p ON p.id = iv.produto_id "
                + "JOIN categoria c ON c.id = p.categoria_id "
                + "WHERE v.data_hora >= ? AND v.data_hora < ? "
                + "GROUP BY c.id, c.nome, p.id, p.nome ORDER BY c.nome, qtd DESC, p.nome";

        String sqlSemVenda = "SELECT c.nome, p.nome FROM produto p JOIN categoria c ON c.id = p.categoria_id "
                + "WHERE NOT EXISTS (SELECT 1 FROM item_venda iv JOIN venda v ON v.id = iv.venda_id "
                + "WHERE iv.produto_id = p.id AND v.data_hora >= ? AND v.data_hora < ?) "
                + "ORDER BY c.nome, p.nome";

        try (Connection conn = Conexao.getConexao()) {
            List<VendaProduto> vendidos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlVendidos)) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        vendidos.add(new VendaProduto(rs.getString(1), rs.getString(2), rs.getInt(3)));
                    }
                }
            }
            relatorio.setMaisVendidosPorCategoria(vendidos);

            List<VendaProduto> semVenda = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlSemVenda)) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        semVenda.add(new VendaProduto(rs.getString(1), rs.getString(2), 0));
                    }
                }
            }
            relatorio.setSemVenda(semVenda);

            relatorio.setProdutoMaisVendido(produtoMaisVendido(conn,
                    "v.data_hora >= ? AND v.data_hora < ?", Timestamp.valueOf(inicio), Timestamp.valueOf(fim)));
        }
        return relatorio;
    }

    // RF21: todos os usuários, inclusive os sem venda no período, do que mais vendeu ao que menos vendeu
    public List<VendasVendedor> consultarVendasPorVendedor(LocalDateTime inicio, LocalDateTime fim)
            throws SQLException {
        String sql = "SELECT u.nome, COUNT(v.id), COALESCE(SUM(v.valor_total), 0) AS total "
                + "FROM usuario u LEFT JOIN venda v ON v.usuario_id = u.id "
                + "AND v.data_hora >= ? AND v.data_hora < ? "
                + "GROUP BY u.id, u.nome ORDER BY total DESC, u.nome";

        List<VendasVendedor> vendedores = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            definirPeriodo(stmt, inicio, fim);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vendedores.add(new VendasVendedor(rs.getString(1), rs.getInt(2), rs.getBigDecimal(3)));
                }
            }
        }
        return vendedores;
    }

    // RF22: devoluções do período [inicio, fim)
    public RelatorioDevolucoes consultarDevolucoes(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        RelatorioDevolucoes relatorio = new RelatorioDevolucoes();

        String filtroItens = "FROM item_devolucao idv JOIN devolucao d ON d.id = idv.devolucao_id "
                + "JOIN item_venda iv ON iv.id = idv.item_venda_id "
                + "JOIN produto p ON p.id = iv.produto_id "
                + "JOIN categoria c ON c.id = p.categoria_id "
                + "WHERE d.data_hora >= ? AND d.data_hora < ? ";

        try (Connection conn = Conexao.getConexao()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(valor_reembolsado), 0) FROM devolucao "
                            + "WHERE data_hora >= ? AND data_hora < ?")) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    relatorio.setQuantidadeDevolucoes(rs.getInt(1));
                    relatorio.setValorReembolsado(rs.getBigDecimal(2));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.nome, SUM(idv.quantidade) AS qtd " + filtroItens
                            + "GROUP BY c.id, c.nome ORDER BY qtd DESC, c.nome")) {
                definirPeriodo(stmt, inicio, fim);
                List<RelatorioDevolucoes.CategoriaDevolvida> ranking = new ArrayList<>();
                int totalItens = 0;
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ranking.add(new RelatorioDevolucoes.CategoriaDevolvida(rs.getString(1), rs.getInt(2)));
                        totalItens += rs.getInt(2);
                    }
                }
                relatorio.setRankingCategorias(ranking);
                relatorio.setQuantidadeItens(totalItens);
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT d.status_produto, SUM(idv.quantidade) " + filtroItens + "GROUP BY d.status_produto")) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        relatorio.getItensPorCondicao().put(CondicaoProduto.valueOf(rs.getString(1)), rs.getInt(2));
                    }
                }
            }

            // Predominante só existe quando o motivo mais citado se repete (ou há uma única devolução)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT MIN(TRIM(motivo)), COUNT(*) AS qtd FROM devolucao "
                            + "WHERE data_hora >= ? AND data_hora < ? "
                            + "GROUP BY LOWER(TRIM(motivo)) ORDER BY qtd DESC, 1 LIMIT 1")) {
                definirPeriodo(stmt, inicio, fim);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && (rs.getInt(2) > 1 || relatorio.getQuantidadeDevolucoes() == 1)) {
                        relatorio.setMotivoPredominante(rs.getString(1));
                    }
                }
            }
        }
        return relatorio;
    }

    private ProdutoVendido produtoMaisVendido(Connection conn, String filtro, Object... parametros)
            throws SQLException {
        String sql = "SELECT p.nome, SUM(iv.quantidade) AS qtd FROM item_venda iv "
                + "JOIN venda v ON v.id = iv.venda_id "
                + "JOIN produto p ON p.id = iv.produto_id "
                + "WHERE " + filtro + " GROUP BY p.id, p.nome ORDER BY qtd DESC, p.nome LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? new ProdutoVendido(rs.getString(1), rs.getInt(2)) : null;
            }
        }
    }

    private void definirPeriodo(PreparedStatement stmt, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        stmt.setTimestamp(1, Timestamp.valueOf(inicio));
        stmt.setTimestamp(2, Timestamp.valueOf(fim));
    }
}
